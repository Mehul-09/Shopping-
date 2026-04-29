import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

// ════════════════════════════════════════════════════════════════════════════
//  PART 1 — DATA MODELS  +  DATABASE LAYER
//
//  Contains:
//    • Product          — product data model
//    • CartItem         — cart item data model
//    • DB config        — DB_URL, DB_USER, DB_PASS
//    • isDbAvailable()  — one-time connection test with caching
//    • getConn()        — open a JDBC connection
//    • loadProductsFromDB() / loadFallbackProducts()
//    • registerUser()   — insert new user (DB or offline)
//    • tryLogin()       — authenticate user (DB or offline)
//    • placeOrder()     — transactional order + stock deduction
//    • refreshStock()   — re-read single product stock from DB
//
//  COMPILE (all 4 files together):
//    Windows : javac -cp .;mysql-connector-j-8.x.jar *.java
//    Mac/Linux: javac -cp .:mysql-connector-j-8.x.jar *.java
//
//  RUN:
//    Windows : java -cp .;mysql-connector-j-8.x.jar QuickMartApp
//    Mac/Linux: java -cp .:mysql-connector-j-8.x.jar QuickMartApp
// ════════════════════════════════════════════════════════════════════════════

public class Part1_DatabaseAndModel {

    // ── DB config  ← CHANGE DB_PASS ──────────────────────────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/quickmart"
                                + "?useSSL=false&serverTimezone=UTC"
                                + "&allowPublicKeyRetrieval=true"
                                + "&connectTimeout=3000";
    static final String DB_USER = "root";
    static final String DB_PASS = "your_password_here";   // ← PUT YOUR PASSWORD HERE

    // ── Runtime flags ─────────────────────────────────────────────────────
    static boolean dbAvailable = false;
    static boolean dbChecked   = false;

    // ── In-memory user store (offline fallback) ───────────────────────────
    // Each entry: { email, password, fullName }
    static ArrayList<String[]> offlineUsers = new ArrayList<>();

    // ── Shared app state (referenced across all parts) ────────────────────
    static ArrayList<Product>  products     = new ArrayList<>();
    static ArrayList<CartItem> cart         = new ArrayList<>();
    static int                 loggedUserId = -1;
    static String              loggedName   = "Guest";

    // ════════════════════════════════════════════════════════════════════
    //  MODEL — PRODUCT
    // ════════════════════════════════════════════════════════════════════

    static class Product {
        int    id, stock, price;
        String name, icon, category;

        Product(int id, String name, int price, String icon, String category, int stock) {
            this.id       = id;
            this.name     = name;
            this.price    = price;
            this.icon     = icon;
            this.category = category;
            this.stock    = stock;
        }

        /** True if at least one unit is available. */
        boolean inStock()  { return stock > 0; }

        /** True if stock is critically low (≤ 5 units). */
        boolean lowStock() { return stock > 0 && stock <= 5; }
    }

    // ════════════════════════════════════════════════════════════════════
    //  MODEL — CART ITEM
    // ════════════════════════════════════════════════════════════════════

    static class CartItem {
        Product p;
        int     qty = 1;

        CartItem(Product p) { this.p = p; }
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATABASE — CONNECTION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Tests the DB connection once and caches the result.
     * All subsequent calls return the cached flag instantly.
     */
    static boolean isDbAvailable() {
        if (dbChecked) return dbAvailable;
        dbChecked = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                dbAvailable = cn.isValid(2);
            }
        } catch (Exception e) {
            dbAvailable = false;
            System.out.println("[INFO] MySQL unavailable — running offline. Reason: " + e.getMessage());
        }
        return dbAvailable;
    }

    /** Open a fresh JDBC connection (caller must close it). */
    static Connection getConn() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATABASE — PRODUCTS
    // ════════════════════════════════════════════════════════════════════

    /**
     * Clears the products list and reloads from MySQL.
     * Falls back to hard-coded demo data if the DB is unavailable.
     */
    static void loadProductsFromDB() {
        products.clear();
        if (isDbAvailable()) {
            String sql =
                "SELECT p.id, p.name, p.icon, c.name AS category, p.price, p.stock_qty " +
                "FROM   products p " +
                "JOIN   categories c ON c.id = p.category_id " +
                "WHERE  p.is_active = 1 " +
                "ORDER  BY c.name, p.name";
            try (Connection cn = getConn();
                 Statement  st = cn.createStatement();
                 ResultSet  rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("price"),
                        rs.getString("icon"),
                        rs.getString("category"),
                        rs.getInt("stock_qty")
                    ));
                }
                return;   // success — skip fallback
            } catch (SQLException ex) {
                System.err.println("[DB] loadProducts error: " + ex.getMessage());
            }
        }
        loadFallbackProducts();
    }

    /** Hard-coded product list used when the database is unreachable. */
    static void loadFallbackProducts() {
        Object[][] data = {
            {1,  "Whole Milk",      52,  "🥛", "Dairy",   50},
            {2,  "Sourdough",       38,  "🍞", "Bakery",  30},
            {3,  "Farm Eggs",       75,  "🥚", "Dairy",   40},
            {4,  "Red Apple",       110, "🍎", "Fruits",  60},
            {5,  "Chips",           45,  "🍟", "Snacks",  80},
            {6,  "Dark Choco",      130, "🍫", "Snacks",  3 },
            {7,  "Orange Juice",    85,  "🧃", "Drinks",  35},
            {8,  "Butter",          65,  "🧈", "Dairy",   0 },
            {9,  "Biscuits",        38,  "🍪", "Snacks",  90},
            {10, "Sparkling Water", 25,  "💧", "Drinks",  100}
        };
        for (Object[] row : data) {
            products.add(new Product(
                (int)    row[0],
                (String) row[1],
                (int)    row[2],
                (String) row[3],
                (String) row[4],
                (int)    row[5]
            ));
        }
    }

    /**
     * Re-fetches the stock count for a single product from MySQL.
     * No-op when offline (stock stays as loaded in memory).
     */
    static void refreshStock(Product p) {
        if (!isDbAvailable()) return;
        String sql = "SELECT stock_qty FROM products WHERE id = ?";
        try (Connection        cn = getConn();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, p.id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.stock = rs.getInt(1);
            }
        } catch (SQLException ignored) {}
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATABASE — AUTH : REGISTER
    // ════════════════════════════════════════════════════════════════════

    /**
     * Registers a new user.
     *
     * @return null on success, or a user-facing error message string.
     *
     * Online  → validates + inserts into MySQL users table.
     * Offline → validates + stores in the offlineUsers ArrayList.
     */
    static String registerUser(String fullName, String email, String password) {

        // ── Input validation ──────────────────────────────────────────
        if (fullName.trim().isEmpty()) return "Full name is required.";
        if (email.trim().isEmpty())    return "Email is required.";
        if (password.isEmpty())        return "Password is required.";
        if (!email.contains("@") || !email.contains("."))
            return "Enter a valid email address.";
        if (password.length() < 4)
            return "Password must be at least 4 characters.";

        // ── Online path ───────────────────────────────────────────────
        if (isDbAvailable()) {
            try (Connection cn = getConn()) {

                // Check for duplicate email
                try (PreparedStatement chk = cn.prepareStatement(
                        "SELECT id FROM users WHERE email = ?")) {
                    chk.setString(1, email.trim().toLowerCase());
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next()) return "An account with this email already exists.";
                    }
                }

                // Insert new user row
                try (PreparedStatement ins = cn.prepareStatement(
                        "INSERT INTO users (username, email, password_hash, full_name) " +
                        "VALUES (?, ?, ?, ?)")) {
                    // Generate a simple unique username from the email prefix
                    String uname = email.split("@")[0] + "_" + (System.currentTimeMillis() % 9999);
                    ins.setString(1, uname);
                    ins.setString(2, email.trim().toLowerCase());
                    ins.setString(3, password);      // ⚠ use bcrypt in production
                    ins.setString(4, fullName.trim());
                    ins.executeUpdate();
                }
                return null;   // success

            } catch (SQLException ex) {
                return "Database error: " + ex.getMessage();
            }
        }

        // ── Offline path ──────────────────────────────────────────────
        for (String[] u : offlineUsers) {
            if (u[0].equalsIgnoreCase(email.trim()))
                return "An account with this email already exists.";
        }
        offlineUsers.add(new String[]{
            email.trim().toLowerCase(),
            password,
            fullName.trim()
        });
        return null;   // success
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATABASE — AUTH : LOGIN
    // ════════════════════════════════════════════════════════════════════

    /**
     * Attempts to log in with the given credentials.
     *
     * @return The user's DB id (> 0) on success, or -1 on failure.
     *         Also sets the global {@code loggedName} on success.
     *
     * Online  → queries the users table.
     * Offline → checks the offlineUsers list (includes the seeded demo account).
     */
    static int tryLogin(String email, String password) {
        if (email.trim().isEmpty() || password.isEmpty()) return -1;

        // ── Online path ───────────────────────────────────────────────
        if (isDbAvailable()) {
            String sql = "SELECT id, full_name " +
                         "FROM   users " +
                         "WHERE  email = ? AND password_hash = ?";
            try (Connection        cn = getConn();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, email.trim().toLowerCase());
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        loggedName = rs.getString("full_name");
                        if (loggedName == null || loggedName.isEmpty()) loggedName = email;
                        return rs.getInt("id");
                    }
                }
            } catch (SQLException ex) {
                System.err.println("[DB] login error: " + ex.getMessage());
                // fall through to offline check as a safety net
            }
        }

        // ── Offline path (also covers in-memory registered users) ─────
        for (String[] u : offlineUsers) {
            if (u[0].equalsIgnoreCase(email.trim()) && u[1].equals(password)) {
                loggedName = u[2];
                return 1;   // synthetic id for offline session
            }
        }
        return -1;
    }

    // ════════════════════════════════════════════════════════════════════
    //  DATABASE — ORDER PLACEMENT
    // ════════════════════════════════════════════════════════════════════

    /**
     * Places the current cart as a paid order.
     *
     * Online  → full transaction: inserts order header, calls
     *           sp_deduct_stock for each item, inserts order_items.
     *           Rolls back if any item's stock is insufficient.
     * Offline → deducts stock from in-memory product objects only.
     *
     * @return null on success, or a user-facing error string.
     */
    static String placeOrder(String paymentMethod) {

        // ── Offline path ──────────────────────────────────────────────
        if (!isDbAvailable()) {
            for (CartItem ci : cart)
                if (ci.p.stock < ci.qty)
                    return ci.p.name + " has insufficient stock.";
            for (CartItem ci : cart)
                ci.p.stock -= ci.qty;
            return null;
        }

        // ── Online path ───────────────────────────────────────────────
        try (Connection cn = getConn()) {
            cn.setAutoCommit(false);

            // 1. Insert order header
            int orderId;
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO orders " +
                    "(user_id, total_amount, payment_method, status) " +
                    "VALUES (?, ?, ?, 'paid')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, loggedUserId < 1 ? 1 : loggedUserId);
                ps.setBigDecimal(2, new BigDecimal(cartTotal()));
                ps.setString(3, paymentMethod);
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    k.next();
                    orderId = k.getInt(1);
                }
            }

            // 2. Deduct stock (stored procedure) + insert order_items
            try (CallableStatement cs = cn.prepareCall(
                         "{CALL sp_deduct_stock(?, ?, ?, ?)}");
                 PreparedStatement ip = cn.prepareStatement(
                         "INSERT INTO order_items " +
                         "(order_id, product_id, quantity, unit_price) " +
                         "VALUES (?, ?, ?, ?)")) {

                for (CartItem ci : cart) {
                    cs.setInt(1, ci.p.id);
                    cs.setInt(2, ci.qty);
                    cs.setInt(3, orderId);
                    cs.registerOutParameter(4, Types.TINYINT);
                    cs.execute();

                    if (cs.getInt(4) == 0) {
                        cn.rollback();
                        return ci.p.name + " ran out of stock. Order cancelled.";
                    }

                    ip.setInt(1, orderId);
                    ip.setInt(2, ci.p.id);
                    ip.setInt(3, ci.qty);
                    ip.setBigDecimal(4, new BigDecimal(ci.p.price));
                    ip.executeUpdate();
                }
            }

            cn.commit();
            return null;   // success

        } catch (SQLException ex) {
            return "Database error: " + ex.getMessage();
        }
    }

    // ── Helper used by placeOrder (also accessible from Part 3) ──────────
    static int cartTotal() {
        int t = 0;
        for (CartItem c : cart) t += c.p.price * c.qty;
        return t;
    }
}
