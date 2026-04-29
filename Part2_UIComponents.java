import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.Border;

// ════════════════════════════════════════════════════════════════════════════
//  PART 2 — UI COMPONENT FACTORY  +  COLOUR PALETTE
//
//  Contains:
//    • All Color constants (BG, SURFACE, ACCENT, SUCCESS, DANGER …)
//    • gradientBtn()   — filled gradient JButton
//    • accentBtn()     — indigo gradient shortcut
//    • successBtn()    — emerald gradient shortcut
//    • dangerBtn()     — rose gradient shortcut (disabled state)
//    • ghostBtn()      — outlined / transparent JButton
//    • roundIconBtn()  — circular icon button (qty +/−)
//    • darkField()     — styled JTextField with rounded border
//    • darkPass()      — styled JPasswordField with rounded border
//    • roundPanel()    — panel with rounded corners + drop shadow
//    • lbl()           — quick JLabel factory (text, size, style, colour)
//    • stockBadge()    — coloured pill label for product stock status
//    • brandingPanel() — shared left-side branding column
//    • navBar()        — top navigation bar with optional cart button
// ════════════════════════════════════════════════════════════════════════════

public class Part2_UIComponents {

    // ════════════════════════════════════════════════════════════════════
    //  COLOUR PALETTE
    // ════════════════════════════════════════════════════════════════════

    static final Color BG           = new Color(15,  17,  26);
    static final Color SURFACE      = new Color(24,  27,  42);
    static final Color SURFACE2     = new Color(34,  38,  58);
    static final Color BORDER       = new Color(50,  55,  82);
    static final Color ACCENT       = new Color(99,  102, 241);
    static final Color ACCENT2      = new Color(139, 92,  246);
    static final Color SUCCESS      = new Color(16,  185, 129);
    static final Color SUCCESS2     = new Color(5,   150, 105);
    static final Color DANGER       = new Color(244, 63,  94);
    static final Color DANGER2      = new Color(200, 30,  60);
    static final Color WARNING      = new Color(251, 146, 60);
    static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    static final Color TEXT_MUTED   = new Color(100, 116, 139);
    static final Color TEXT_DIM     = new Color(148, 163, 184);
    static final Color GOLD         = new Color(251, 191, 36);

    // ════════════════════════════════════════════════════════════════════
    //  BUTTONS
    // ════════════════════════════════════════════════════════════════════

    /**
     * Base gradient button.
     *
     * @param text    button label
     * @param c1      gradient start colour
     * @param c2      gradient end colour
     * @param enabled whether the button starts enabled
     */
    static JButton gradientBtn(String text, Color c1, Color c2, boolean enabled) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                // Background fill
                if (isEnabled()) {
                    g2.setPaint(new GradientPaint(0, 0, c1,
                                                  getWidth(), getHeight(), c2));
                } else {
                    g2.setColor(SURFACE2);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0,
                        getWidth(), getHeight(), 12, 12));

                // Hover overlay
                if (getModel().isRollover() && isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 28));
                    g2.fill(new RoundRectangle2D.Float(0, 0,
                            getWidth(), getHeight(), 12, 12));
                }

                // Pressed overlay
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fill(new RoundRectangle2D.Float(0, 0,
                            getWidth(), getHeight(), 12, 12));
                }

                // Label text
                g2.setColor(isEnabled() ? Color.WHITE : TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setEnabled(enabled);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Indigo-purple gradient — primary action. */
    static JButton accentBtn(String t)  { return gradientBtn(t, ACCENT,  ACCENT2,  true);  }

    /** Emerald gradient — positive / pay action. */
    static JButton successBtn(String t) { return gradientBtn(t, SUCCESS, SUCCESS2, true);  }

    /** Red gradient — disabled "out of stock" display button. */
    static JButton dangerBtn(String t)  { return gradientBtn(t, DANGER,  DANGER2,  false); }

    /**
     * Outlined "ghost" button — secondary actions (Back, Sign Out …).
     * Uses the accent colour for border and text; transparent fill.
     */
    static JButton ghostBtn(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(new Color(99, 102, 241, 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0,
                            getWidth(), getHeight(), 10, 10));
                }

                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1,
                        getWidth() - 2, getHeight() - 2, 10, 10));

                g2.setFont(getFont());
                g2.setColor(ACCENT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * Small circular icon button used for cart quantity +/− controls.
     *
     * @param t  label character ("+" or "−")
     * @param bg circle fill colour
     */
    static JButton roundIconBtn(String t, Color bg) {
        JButton b = new JButton(t) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setPreferredSize(new Dimension(30, 30));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ════════════════════════════════════════════════════════════════════
    //  INPUT FIELDS
    // ════════════════════════════════════════════════════════════════════

    /**
     * Dark-themed text field with rounded border.
     * Border turns accent-coloured when focused.
     *
     * @param placeholder tooltip text shown on hover
     */
    static JTextField darkField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE2);
                g2.fill(new RoundRectangle2D.Float(0, 0,
                        getWidth(), getHeight(), 10, 10));
                g2.setColor(isFocusOwner() ? ACCENT : BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 1, getHeight() - 1, 10, 10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        tf.setOpaque(false);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        tf.setToolTipText(placeholder);
        return tf;
    }

    /**
     * Dark-themed password field matching {@link #darkField}.
     *
     * @param placeholder tooltip text
     */
    static JPasswordField darkPass(String placeholder) {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE2);
                g2.fill(new RoundRectangle2D.Float(0, 0,
                        getWidth(), getHeight(), 10, 10));
                g2.setColor(isFocusOwner() ? ACCENT : BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 1.8f : 1f));
                g2.draw(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 1, getHeight() - 1, 10, 10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        pf.setOpaque(false);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT);
        pf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        pf.setToolTipText(placeholder);
        return pf;
    }

    // ════════════════════════════════════════════════════════════════════
    //  PANELS  +  LABELS
    // ════════════════════════════════════════════════════════════════════

    /**
     * JPanel with rounded corners and a subtle drop shadow.
     *
     * @param bg     fill colour
     * @param radius corner arc radius in pixels
     */
    static JPanel roundPanel(Color bg, int radius) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new RoundRectangle2D.Float(4, 4,
                        getWidth() - 3, getHeight() - 3, radius, radius));

                // Background
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 4, getHeight() - 4, radius, radius));

                // Border
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0,
                        getWidth() - 5, getHeight() - 5, radius, radius));

                g2.dispose();
                super.paintComponent(g);
            }

            @Override public boolean isOpaque() { return false; }
        };
    }

    /**
     * Convenience factory for a styled JLabel.
     *
     * @param t     text content
     * @param sz    font size
     * @param style Font.PLAIN / Font.BOLD / Font.ITALIC
     * @param c     foreground colour
     */
    static JLabel lbl(String t, int sz, int style, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", style, sz));
        l.setForeground(c);
        return l;
    }

    /**
     * Pill-shaped stock status badge for a product card.
     * Green = in stock, amber = low stock (≤ 5), red = out of stock.
     */
    static JLabel stockBadge(Part1_DatabaseAndModel.Product p) {
        String txt;
        Color  col;

        if (!p.inStock())      { txt = "Out of Stock";              col = DANGER;  }
        else if (p.lowStock()) { txt = "Only " + p.stock + " left"; col = WARNING; }
        else                   { txt = "In Stock · " + p.stock;     col = SUCCESS; }

        return new JLabel(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                // Tinted background fill
                g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Border
                g2.setColor(col);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                // Text
                g2.setFont(getFont());
                g2.setColor(col);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth()  - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            @Override public boolean isOpaque() { return false; }
        };
    }

    // ════════════════════════════════════════════════════════════════════
    //  BRANDING PANEL  (shared left column on Login / Register screens)
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the dark left-side branding strip shown on the auth screens.
     * Displays the logo, tagline, DB status indicator, and feature bullets.
     */
    static JPanel brandingPanel() {
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(new Color(18, 20, 34));
        left.setPreferredSize(new Dimension(300, 0));
        left.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);

        JLabel bolt = lbl("⚡", 52, Font.PLAIN, GOLD);             bolt.setAlignmentX(0);
        JLabel name = lbl("QuickMart", 28, Font.BOLD, TEXT_PRIMARY); name.setAlignmentX(0);
        JLabel t1   = lbl("Fresh groceries.", 13, Font.PLAIN, TEXT_MUTED); t1.setAlignmentX(0);
        JLabel t2   = lbl("Delivered fast.",  13, Font.PLAIN, TEXT_DIM);   t2.setAlignmentX(0);

        brand.add(bolt); brand.add(Box.createVerticalStrut(12));
        brand.add(name); brand.add(Box.createVerticalStrut(8));
        brand.add(t1);   brand.add(t2);
        brand.add(Box.createVerticalStrut(22));

        // DB connection status pill
        boolean   dbUp  = Part1_DatabaseAndModel.isDbAvailable();
        Color     dbCol = dbUp ? SUCCESS : WARNING;
        String    dbTxt = dbUp ? "🟢 MySQL Connected" : "🟡 Offline Mode";
        JLabel dbLbl = lbl(dbTxt, 11, Font.BOLD, dbCol); dbLbl.setAlignmentX(0);
        brand.add(dbLbl); brand.add(Box.createVerticalStrut(18));

        // Feature bullets
        for (String f : new String[]{
                "✓ Live Stock Tracking",
                "✓ Instant Checkout",
                "✓ Secure Auth"}) {
            JLabel fl = lbl(f, 12, Font.PLAIN, new Color(99, 102, 241, 200));
            fl.setAlignmentX(0);
            brand.add(fl);
            brand.add(Box.createVerticalStrut(6));
        }

        left.add(brand);
        return left;
    }

    // ════════════════════════════════════════════════════════════════════
    //  NAV BAR
    // ════════════════════════════════════════════════════════════════════

    /**
     * Builds the top navigation bar.
     *
     * @param title     optional centre title (pass null or "" to omit)
     * @param showCart  if true, renders the animated cart count button
     */
    static JPanel navBar(String title, boolean showCart) {
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(SURFACE);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
            @Override public boolean isOpaque() { return false; }
        };
        nav.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logo.setOpaque(false);
        logo.add(lbl("⚡", 20, Font.PLAIN, GOLD));
        logo.add(lbl(" QuickMart", 18, Font.BOLD, TEXT_PRIMARY));
        nav.add(logo, BorderLayout.WEST);

        // Centre title
        if (title != null && !title.isEmpty()) {
            JLabel tl = lbl(title, 13, Font.PLAIN, TEXT_MUTED);
            tl.setHorizontalAlignment(JLabel.CENTER);
            nav.add(tl, BorderLayout.CENTER);
        }

        // Cart button
        if (showCart) {
            int count = QuickMartApp.cartItemCount();
            JButton cartBtn = new JButton("🛒  Cart  " + count) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover() ? SUCCESS.brighter() : SUCCESS);
                    g2.fill(new RoundRectangle2D.Float(0, 0,
                            getWidth(), getHeight(), 20, 20));
                    g2.setColor(Color.WHITE);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(),
                            (getWidth()  - fm.stringWidth(getText())) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            cartBtn.setOpaque(false);
            cartBtn.setContentAreaFilled(false);
            cartBtn.setBorderPainted(false);
            cartBtn.setFocusPainted(false);
            cartBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
            cartBtn.setPreferredSize(new Dimension(145, 36));
            cartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cartBtn.addActionListener(e -> QuickMartApp.showCart());

            // Store reference so other parts can update the label
            QuickMartApp.cartNavBtn = cartBtn;
            nav.add(cartBtn, BorderLayout.EAST);
        }
        return nav;
    }
}
