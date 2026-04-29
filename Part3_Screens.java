import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import javax.swing.*;

// ════════════════════════════════════════════════════════════════════════════
//  PART 3 — SCREENS  (Login · Register · Home · Shop · Cart)
//
//  Every method returns a fully built JPanel that is added to the
//  CardLayout container in QuickMartApp (Part 4).
//
//  Screen routing is done via QuickMartApp.layout.show(…).
//  Shared state (products, cart, loggedName …) lives in Part 1.
//  All widget factories live in Part 2.
// ════════════════════════════════════════════════════════════════════════════

public class Part3_Screens {

    // Colour aliases (just for readability — resolved from Part2 at runtime)
    static final Color BG           = Part2_UIComponents.BG;
    static final Color SURFACE      = Part2_UIComponents.SURFACE;
    static final Color SURFACE2     = Part2_UIComponents.SURFACE2;
    static final Color BORDER       = Part2_UIComponents.BORDER;
    static final Color ACCENT       = Part2_UIComponents.ACCENT;
    static final Color SUCCESS      = Part2_UIComponents.SUCCESS;
    static final Color DANGER       = Part2_UIComponents.DANGER;
    static final Color WARNING      = Part2_UIComponents.WARNING;
    static final Color TEXT_PRIMARY = Part2_UIComponents.TEXT_PRIMARY;
    static final Color TEXT_MUTED   = Part2_UIComponents.TEXT_MUTED;
    static final Color TEXT_DIM     = Part2_UIComponents.TEXT_DIM;
    static final Color GOLD         = Part2_UIComponents.GOLD;

    // ════════════════════════════════════════════════════════════════════
    //  CART HELPERS  (thin wrappers — logic lives in QuickMartApp)
    // ════════════════════════════════════════════════════════════════════

    static void addToCart(Part1_DatabaseAndModel.Product p) {
        Part1_DatabaseAndModel.refreshStock(p);

        if (!p.inStock()) {
            JOptionPane.showMessageDialog(QuickMartApp.frame,
                p.name + " is out of stock.", "Out of Stock",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Part1_DatabaseAndModel.CartItem c : Part1_DatabaseAndModel.cart) {
            if (c.p == p) {
                if (c.qty + 1 > p.stock) {
                    JOptionPane.showMessageDialog(QuickMartApp.frame,
                        "Only " + p.stock + " unit(s) of " + p.name + " available.",
                        "Stock Limit", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                c.qty++;
                QuickMartApp.updateCartNav();
                QuickMartApp.rebuildCart();
                return;
            }
        }

        Part1_DatabaseAndModel.cart.add(new Part1_DatabaseAndModel.CartItem(p));
        QuickMartApp.updateCartNav();
        QuickMartApp.rebuildCart();
    }

    // ════════════════════════════════════════════════════════════════════
    //  SCREEN 1 — LOGIN
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the login screen.
     * Features: email + password fields, inline error label,
     * "Sign In" button (also triggered by Enter in the password field),
     * link to the Register screen, and a demo credential hint.
     */
    static JPanel loginPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.add(Part2_UIComponents.brandingPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(BG);

        JPanel card = Part2_UIComponents.roundPanel(SURFACE, 20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(38, 42, 38, 42));
        card.setPreferredSize(new Dimension(400, 490));

        // ── Labels ───────────────────────────────────────────────────
        JLabel title = Part2_UIComponents.lbl("Welcome back 👋", 22, Font.BOLD, TEXT_PRIMARY);
        title.setAlignmentX(0);
        JLabel sub = Part2_UIComponents.lbl("Sign in to your account", 13, Font.PLAIN, TEXT_MUTED);
        sub.setAlignmentX(0);
        JLabel errorLbl = Part2_UIComponents.lbl("", 12, Font.BOLD, DANGER);
        errorLbl.setAlignmentX(0);

        // ── Input fields ─────────────────────────────────────────────
        JTextField     emailF = Part2_UIComponents.darkField("you@email.com");
        JPasswordField passF  = Part2_UIComponents.darkPass("your password");
        emailF.setAlignmentX(0); emailF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passF.setAlignmentX(0);  passF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // ── Buttons ───────────────────────────────────────────────────
        JButton loginBtn = Part2_UIComponents.accentBtn("Sign In  →");
        loginBtn.setAlignmentX(0);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JButton goReg = Part2_UIComponents.ghostBtn("Create Account  →");
        goReg.setAlignmentX(0);
        goReg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel noAcct = Part2_UIComponents.lbl("Don't have an account?", 12, Font.PLAIN, TEXT_MUTED);
        noAcct.setAlignmentX(0);
        JLabel hint = Part2_UIComponents.lbl(
            "Default: demo@quickmart.in  /  demo123", 10, Font.PLAIN, TEXT_MUTED);
        hint.setAlignmentX(0);

        // ── Login action ─────────────────────────────────────────────
        ActionListener doLogin = e -> {
            errorLbl.setText("");
            String em = emailF.getText().trim();
            String pw = new String(passF.getPassword());
            if (em.isEmpty() || pw.isEmpty()) {
                errorLbl.setText("✗  Please fill in both fields."); return;
            }
            int uid = Part1_DatabaseAndModel.tryLogin(em, pw);
            if (uid > 0) {
                Part1_DatabaseAndModel.loggedUserId = uid;
                Part1_DatabaseAndModel.loadProductsFromDB();
                QuickMartApp.refreshShopPanel();
                QuickMartApp.refreshHomePanel();
                QuickMartApp.layout.show(QuickMartApp.container, "home");
            } else {
                errorLbl.setText("✗  Incorrect email or password.");
            }
        };
        loginBtn.addActionListener(doLogin);
        passF.addActionListener(doLogin);
        goReg.addActionListener(e ->
            QuickMartApp.layout.show(QuickMartApp.container, "register"));

        // ── Layout ───────────────────────────────────────────────────
        card.add(title);    card.add(Box.createVerticalStrut(4));
        card.add(sub);      card.add(Box.createVerticalStrut(22));
        card.add(errorLbl); card.add(Box.createVerticalStrut(4));
        card.add(Part2_UIComponents.lbl("Email", 11, Font.BOLD, TEXT_DIM));
        card.add(Box.createVerticalStrut(5)); card.add(emailF);
        card.add(Box.createVerticalStrut(14));
        card.add(Part2_UIComponents.lbl("Password", 11, Font.BOLD, TEXT_DIM));
        card.add(Box.createVerticalStrut(5)); card.add(passF);
        card.add(Box.createVerticalStrut(24)); card.add(loginBtn);
        card.add(Box.createVerticalStrut(22)); card.add(sep);
        card.add(Box.createVerticalStrut(16)); card.add(noAcct);
        card.add(Box.createVerticalStrut(8));  card.add(goReg);
        card.add(Box.createVerticalStrut(18)); card.add(hint);

        right.add(card);
        outer.add(right, BorderLayout.CENTER);
        return outer;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SCREEN 2 — REGISTER
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the registration screen.
     * Features: full name, email, password, confirm-password fields,
     * inline error and success labels, auto-login after successful register.
     */
    static JPanel registerPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.add(Part2_UIComponents.brandingPanel(), BorderLayout.WEST);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(BG);

        JPanel card = Part2_UIComponents.roundPanel(SURFACE, 20);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(34, 42, 34, 42));
        card.setPreferredSize(new Dimension(400, 560));

        // ── Labels ───────────────────────────────────────────────────
        JLabel title = Part2_UIComponents.lbl("Create account ✨", 22, Font.BOLD, TEXT_PRIMARY);
        title.setAlignmentX(0);
        JLabel sub = Part2_UIComponents.lbl("Start shopping in seconds", 13, Font.PLAIN, TEXT_MUTED);
        sub.setAlignmentX(0);
        JLabel errorLbl   = Part2_UIComponents.lbl("", 12, Font.BOLD, DANGER);  errorLbl.setAlignmentX(0);
        JLabel successLbl = Part2_UIComponents.lbl("", 12, Font.BOLD, SUCCESS); successLbl.setAlignmentX(0);

        // ── Input fields ─────────────────────────────────────────────
        JTextField     nameF    = Part2_UIComponents.darkField("Full Name");
        JTextField     emailF   = Part2_UIComponents.darkField("you@email.com");
        JPasswordField passF    = Part2_UIComponents.darkPass("Password (min 4 chars)");
        JPasswordField confirmF = Part2_UIComponents.darkPass("Confirm password");
        for (JComponent f : new JComponent[]{nameF, emailF, passF, confirmF}) {
            f.setAlignmentX(0);
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }

        // ── Buttons ───────────────────────────────────────────────────
        JButton regBtn  = Part2_UIComponents.successBtn("Create Account  →");
        regBtn.setAlignmentX(0); regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JButton goLogin = Part2_UIComponents.ghostBtn("Already have an account? Sign In");
        goLogin.setAlignmentX(0); goLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ── Register action ───────────────────────────────────────────
        ActionListener doRegister = e -> {
            errorLbl.setText(""); successLbl.setText("");
            String fn  = nameF.getText().trim();
            String em  = emailF.getText().trim();
            String pw  = new String(passF.getPassword());
            String cpw = new String(confirmF.getPassword());

            if (!pw.equals(cpw)) {
                errorLbl.setText("✗  Passwords do not match."); return;
            }
            String err = Part1_DatabaseAndModel.registerUser(fn, em, pw);
            if (err != null) {
                errorLbl.setText("✗  " + err);
            } else {
                successLbl.setText("✅  Account created! Signing you in…");
                regBtn.setEnabled(false);
                new javax.swing.Timer(900, ev -> {
                    int uid = Part1_DatabaseAndModel.tryLogin(em, pw);
                    if (uid > 0) {
                        Part1_DatabaseAndModel.loggedUserId = uid;
                        Part1_DatabaseAndModel.loadProductsFromDB();
                        QuickMartApp.refreshShopPanel();
                        QuickMartApp.refreshHomePanel();
                        QuickMartApp.layout.show(QuickMartApp.container, "home");
                    }
                    regBtn.setEnabled(true);
                }) {{ setRepeats(false); }}.start();
            }
        };
        regBtn.addActionListener(doRegister);
        confirmF.addActionListener(doRegister);
        goLogin.addActionListener(e ->
            QuickMartApp.layout.show(QuickMartApp.container, "login"));

        // ── Layout ───────────────────────────────────────────────────
        card.add(title);      card.add(Box.createVerticalStrut(4));
        card.add(sub);        card.add(Box.createVerticalStrut(14));
        card.add(errorLbl);   card.add(Box.createVerticalStrut(2));
        card.add(successLbl); card.add(Box.createVerticalStrut(4));
        card.add(Part2_UIComponents.lbl("Full Name",        11, Font.BOLD, TEXT_DIM)); card.add(Box.createVerticalStrut(5)); card.add(nameF);
        card.add(Box.createVerticalStrut(12));
        card.add(Part2_UIComponents.lbl("Email Address",    11, Font.BOLD, TEXT_DIM)); card.add(Box.createVerticalStrut(5)); card.add(emailF);
        card.add(Box.createVerticalStrut(12));
        card.add(Part2_UIComponents.lbl("Password",         11, Font.BOLD, TEXT_DIM)); card.add(Box.createVerticalStrut(5)); card.add(passF);
        card.add(Box.createVerticalStrut(12));
        card.add(Part2_UIComponents.lbl("Confirm Password", 11, Font.BOLD, TEXT_DIM)); card.add(Box.createVerticalStrut(5)); card.add(confirmF);
        card.add(Box.createVerticalStrut(22)); card.add(regBtn);
        card.add(Box.createVerticalStrut(18)); card.add(sep);
        card.add(Box.createVerticalStrut(14)); card.add(goLogin);

        right.add(card);
        outer.add(right, BorderLayout.CENTER);
        return outer;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SCREEN 3 — HOME
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the home / dashboard screen.
     * Displays a personalised greeting and three navigation tiles:
     * Browse Shop, My Cart, and Sync Stock (forces a DB refresh).
     */
    static JPanel homePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setName("home"); p.setBackground(BG);
        p.add(Part2_UIComponents.navBar(null, false), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout()); center.setBackground(BG);
        JPanel inner  = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        // Greeting
        String first = Part1_DatabaseAndModel.loggedName.split(" ")[0];
        JLabel greet = Part2_UIComponents.lbl("Hello, " + first + "! 🌿",
                        28, Font.BOLD, TEXT_PRIMARY);
        greet.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = Part2_UIComponents.lbl("Fresh groceries, one tap away.",
                      14, Font.PLAIN, TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Navigation tiles
        JPanel tiles = new JPanel(new GridLayout(1, 3, 18, 0));
        tiles.setOpaque(false);
        tiles.setMaximumSize(new Dimension(700, 135));
        tiles.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel shopTile = homeTile("🛍", "Browse Shop",  "Explore fresh items",   ACCENT);
        JPanel cartTile = homeTile("🛒", "My Cart",      "View & checkout",         SUCCESS);
        JPanel syncTile = homeTile("🔄", "Sync Stock",   "Refresh from database",  new Color(6, 182, 212));

        shopTile.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                QuickMartApp.layout.show(QuickMartApp.container, "shop");
            }
        });
        cartTile.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { QuickMartApp.showCart(); }
        });
        syncTile.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Part1_DatabaseAndModel.dbChecked = false;
                Part1_DatabaseAndModel.loadProductsFromDB();
                QuickMartApp.refreshShopPanel();
                JOptionPane.showMessageDialog(QuickMartApp.frame,
                    Part1_DatabaseAndModel.isDbAvailable()
                        ? "Stock refreshed from MySQL ✓"
                        : "Offline — demo stock reloaded.",
                    "Sync", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        tiles.add(shopTile); tiles.add(cartTile); tiles.add(syncTile);

        // DB mode indicator
        boolean db = Part1_DatabaseAndModel.isDbAvailable();
        JLabel modeLbl = Part2_UIComponents.lbl(
            db ? "🟢 MySQL Connected" : "🟡 Offline Mode — data stored in memory",
            11, Font.PLAIN, db ? SUCCESS : WARNING);
        modeLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sign Out
        JButton logout = Part2_UIComponents.ghostBtn("Sign Out");
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.addActionListener(e -> {
            Part1_DatabaseAndModel.loggedUserId = -1;
            Part1_DatabaseAndModel.loggedName   = "Guest";
            Part1_DatabaseAndModel.cart.clear();
            QuickMartApp.updateCartNav();
            QuickMartApp.layout.show(QuickMartApp.container, "login");
        });

        inner.add(greet);   inner.add(Box.createVerticalStrut(6));
        inner.add(sub);     inner.add(Box.createVerticalStrut(32));
        inner.add(tiles);   inner.add(Box.createVerticalStrut(24));
        inner.add(modeLbl); inner.add(Box.createVerticalStrut(16));
        inner.add(logout);

        center.add(inner);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    /** Builds a single clickable tile for the home dashboard. */
    private static JPanel homeTile(String icon, String title, String sub, Color accent) {
        JPanel tile = new JPanel(new BorderLayout()) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth()-3, getHeight()-3, 18, 18));
                // Background
                g2.setColor(hover ? Part2_UIComponents.SURFACE2 : Part2_UIComponents.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-3, 18, 18));
                // Top accent stripe
                g2.setPaint(new GradientPaint(0, 0, accent, getWidth(), 0, accent.darker()));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-3, 7, 6, 6));
                // Border
                g2.setColor(hover ? accent : Part2_UIComponents.BORDER);
                g2.setStroke(new BasicStroke(hover ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-4, getHeight()-4, 18, 18));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isOpaque() { return false; }
        };
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tile.setBorder(BorderFactory.createEmptyBorder(20, 18, 18, 18));

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        text.add(Part2_UIComponents.lbl(icon,  30, Font.PLAIN, TEXT_PRIMARY));
        text.add(Box.createVerticalStrut(10));
        text.add(Part2_UIComponents.lbl(title, 14, Font.BOLD,  TEXT_PRIMARY));
        text.add(Box.createVerticalStrut(3));
        text.add(Part2_UIComponents.lbl(sub,   11, Font.PLAIN, TEXT_MUTED));
        tile.add(text, BorderLayout.CENTER);
        return tile;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SCREEN 4 — SHOP
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the product grid screen.
     * Displays all active products as cards with stock badges.
     * Shows a summary bar at the bottom with product / out-of-stock counts.
     */
    static JPanel shopPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setName("shop"); p.setBackground(BG);
        p.add(Part2_UIComponents.navBar("Shop", true), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 4, 14, 14));
        grid.setBackground(BG);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        for (Part1_DatabaseAndModel.Product pr : Part1_DatabaseAndModel.products)
            grid.add(productCard(pr));

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        p.add(scroll, BorderLayout.CENTER);

        // Bottom status bar
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Part2_UIComponents.SURFACE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)));

        JButton back = Part2_UIComponents.ghostBtn("← Home");
        back.addActionListener(e ->
            QuickMartApp.layout.show(QuickMartApp.container, "home"));

        long oos = Part1_DatabaseAndModel.products.stream()
                       .filter(pr -> !pr.inStock()).count();
        long low = Part1_DatabaseAndModel.products.stream()
                       .filter(Part1_DatabaseAndModel.Product::lowStock).count();
        JLabel stat = Part2_UIComponents.lbl(
            Part1_DatabaseAndModel.products.size() + " products · " +
            oos + " out of stock · " + low + " low stock",
            11, Font.PLAIN, TEXT_MUTED);

        bar.add(back, BorderLayout.WEST);
        bar.add(stat, BorderLayout.CENTER);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    /** Builds a single product card for the shop grid. */
    private static JPanel productCard(Part1_DatabaseAndModel.Product pr) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth()-2, getHeight()-2, 16, 16));
                g2.setColor(hover ? Part2_UIComponents.SURFACE2 : Part2_UIComponents.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-3, getHeight()-3, 16, 16));
                Color bc = !pr.inStock() ? new Color(244,63,94,100)
                         : pr.lowStock() ? new Color(251,146,60,120)
                         : hover ? ACCENT : BORDER;
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(!pr.inStock() || hover ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-4, getHeight()-4, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public boolean isOpaque() { return false; }
        };
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Content
        JLabel cat    = Part2_UIComponents.lbl(pr.category, 10, Font.BOLD, TEXT_MUTED);
        JLabel ico    = Part2_UIComponents.lbl(pr.icon, 34, Font.PLAIN, TEXT_PRIMARY);
        ico.setHorizontalAlignment(JLabel.CENTER);
        JLabel nameL  = Part2_UIComponents.lbl(pr.name, 13, Font.BOLD,
                            pr.inStock() ? TEXT_PRIMARY : TEXT_MUTED);
        nameL.setHorizontalAlignment(JLabel.CENTER);
        JLabel priceL = Part2_UIComponents.lbl("₹ " + pr.price, 14, Font.BOLD,
                            Part2_UIComponents.GOLD);
        priceL.setHorizontalAlignment(JLabel.CENTER);
        JLabel badge  = Part2_UIComponents.stockBadge(pr);
        badge.setHorizontalAlignment(JLabel.CENTER);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));

        JPanel info = new JPanel(new GridLayout(4, 1, 2, 3));
        info.setOpaque(false);
        info.add(ico); info.add(nameL); info.add(priceL); info.add(badge);

        // Add / Out-of-stock button
        JButton addBtn;
        if (!pr.inStock()) {
            addBtn = Part2_UIComponents.dangerBtn("Out of Stock");
        } else {
            addBtn = Part2_UIComponents.accentBtn("+ Add");
            addBtn.addActionListener(e -> {
                addToCart(pr);
                addBtn.setText("✓ Added"); addBtn.setEnabled(false);
                new javax.swing.Timer(900, ev -> {
                    addBtn.setText("+ Add"); addBtn.setEnabled(true);
                }) {{ setRepeats(false); }}.start();
            });
        }
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addBtn.setPreferredSize(new Dimension(0, 34));

        card.add(cat,    BorderLayout.NORTH);
        card.add(info,   BorderLayout.CENTER);
        card.add(addBtn, BorderLayout.SOUTH);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SCREEN 5 — CART
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the shopping cart screen.
     * Shows either an "empty cart" state or a scrollable list of CartRow items.
     * Footer shows the running total and Checkout / Back buttons.
     */
    static JPanel cartPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(Part2_UIComponents.navBar("My Cart", false), BorderLayout.NORTH);

        if (Part1_DatabaseAndModel.cart.isEmpty()) {
            // ── Empty state ───────────────────────────────────────────
            JPanel empty = new JPanel(new GridBagLayout()); empty.setBackground(BG);
            JPanel box = new JPanel(); box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS)); box.setOpaque(false);

            JLabel ico = Part2_UIComponents.lbl("🛒", 50, Font.PLAIN, TEXT_MUTED); ico.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel msg = Part2_UIComponents.lbl("Your cart is empty", 18, Font.BOLD, Part2_UIComponents.TEXT_DIM); msg.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel sub = Part2_UIComponents.lbl("Add some items from the shop.", 13, Font.PLAIN, TEXT_MUTED); sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            JButton gs = Part2_UIComponents.accentBtn("  Browse Shop  "); gs.setAlignmentX(Component.CENTER_ALIGNMENT);
            gs.addActionListener(e -> QuickMartApp.layout.show(QuickMartApp.container, "shop"));

            box.add(ico); box.add(Box.createVerticalStrut(14));
            box.add(msg); box.add(Box.createVerticalStrut(6));
            box.add(sub); box.add(Box.createVerticalStrut(22)); box.add(gs);
            empty.add(box); main.add(empty, BorderLayout.CENTER);

        } else {
            // ── Item list ─────────────────────────────────────────────
            JPanel list = new JPanel(); list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(BG); list.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            for (Part1_DatabaseAndModel.CartItem c :
                    new ArrayList<>(Part1_DatabaseAndModel.cart)) {
                list.add(cartRow(c)); list.add(Box.createVerticalStrut(10));
            }
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null); scroll.setBackground(BG);
            scroll.getViewport().setBackground(BG);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            main.add(scroll, BorderLayout.CENTER);
        }

        // ── Footer bar ────────────────────────────────────────────────
        int total = Part1_DatabaseAndModel.cartTotal();
        JPanel bar = new JPanel(new BorderLayout(16, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Part2_UIComponents.SURFACE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER); g.fillRect(0, 0, getWidth(), 1);
                super.paintComponent(g);
            }
            @Override public boolean isOpaque() { return false; }
        };
        bar.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel totals = new JPanel(); totals.setLayout(new BoxLayout(totals, BoxLayout.Y_AXIS)); totals.setOpaque(false);
        int cnt = QuickMartApp.cartItemCount();
        totals.add(Part2_UIComponents.lbl(cnt + " item" + (cnt == 1 ? "" : "s") + " in cart",
                    12, Font.PLAIN, TEXT_MUTED));
        totals.add(Box.createVerticalStrut(3));
        totals.add(Part2_UIComponents.lbl("₹ " + total, 24, Font.BOLD, TEXT_PRIMARY));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); btns.setOpaque(false);
        JButton back     = Part2_UIComponents.ghostBtn("← Shop");
        JButton checkout = Part2_UIComponents.accentBtn("Checkout  →");
        checkout.setPreferredSize(new Dimension(160, 40));
        checkout.setEnabled(!Part1_DatabaseAndModel.cart.isEmpty());

        back.addActionListener(e -> QuickMartApp.layout.show(QuickMartApp.container, "shop"));
        checkout.addActionListener(e -> {
            QuickMartApp.removeCard("payment");
            JPanel pay = Part4_Payment.paymentPanel(total);
            pay.setName("payment");
            QuickMartApp.container.add(pay, "payment");
            QuickMartApp.layout.show(QuickMartApp.container, "payment");
        });

        btns.add(back); btns.add(checkout);
        bar.add(totals, BorderLayout.WEST); bar.add(btns, BorderLayout.EAST);
        main.add(bar, BorderLayout.SOUTH);
        return main;
    }

    /** Builds a single row widget for an item in the cart. */
    private static JPanel cartRow(Part1_DatabaseAndModel.CartItem c) {
        JPanel row = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,0,0,45));
                g2.fill(new RoundRectangle2D.Float(3,3,getWidth()-2,getHeight()-2,14,14));
                g2.setColor(Part2_UIComponents.SURFACE);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth()-3,getHeight()-3,14,14));
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-4,getHeight()-4,14,14));
                g2.dispose(); super.paintComponent(g);
            }
            @Override public boolean isOpaque(){return false;}
        };
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel ico  = Part2_UIComponents.lbl(c.p.icon, 26, Font.PLAIN, TEXT_PRIMARY);

        JPanel info = new JPanel(); info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS)); info.setOpaque(false);
        info.add(Part2_UIComponents.lbl(c.p.name, 14, Font.BOLD, TEXT_PRIMARY));
        info.add(Box.createVerticalStrut(3));
        String stkNote = c.p.stock < c.qty ? "  ⚠ only " + c.p.stock + " in stock" : "";
        info.add(Part2_UIComponents.lbl("₹" + c.p.price + " each" + stkNote,
            12, Font.PLAIN, c.p.stock < c.qty ? WARNING : TEXT_MUTED));

        JButton minus = Part2_UIComponents.roundIconBtn("−", DANGER);
        JLabel  qtyL  = Part2_UIComponents.lbl("" + c.qty, 14, Font.BOLD, TEXT_PRIMARY);
        qtyL.setHorizontalAlignment(JLabel.CENTER); qtyL.setPreferredSize(new Dimension(30, 30));
        JButton plus  = Part2_UIComponents.roundIconBtn("+", SUCCESS);
        JLabel  lineT = Part2_UIComponents.lbl("₹" + (c.p.price * c.qty), 14, Font.BOLD,
                            Part2_UIComponents.GOLD);
        lineT.setPreferredSize(new Dimension(70, 30)); lineT.setHorizontalAlignment(JLabel.RIGHT);

        minus.addActionListener(e -> {
            c.qty--; if (c.qty <= 0) Part1_DatabaseAndModel.cart.remove(c);
            QuickMartApp.showCart();
        });
        plus.addActionListener(e -> {
            if (c.qty + 1 > c.p.stock) {
                JOptionPane.showMessageDialog(QuickMartApp.frame,
                    "Only " + c.p.stock + " unit(s) available.",
                    "Stock Limit", JOptionPane.WARNING_MESSAGE);
            } else { c.qty++; QuickMartApp.showCart(); }
        });

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); ctrl.setOpaque(false);
        ctrl.add(minus); ctrl.add(qtyL); ctrl.add(plus);
        ctrl.add(Box.createHorizontalStrut(10)); ctrl.add(lineT);

        row.add(ico,  BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.add(ctrl, BorderLayout.EAST);
        return row;
    }
}
