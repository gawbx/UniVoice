package univoice.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Colors, fonts, and small styling helpers shared by every screen.
 *
 * The palette follows Batangas State University's red, white, and gray.
 * Students get the LIGHT mode and administrators get the DARK mode. The
 * color fields are swapped by {@link #apply(Mode)} before a screen is built,
 * and the custom-painted components read them every time they repaint.
 */
public final class Theme {

    public enum Mode { LIGHT, DARK }

    /** BatStateU red, used as the brand color in both modes. */
    public static final Color BSU_RED = new Color(0xC8102E);
    public static final Color WHITE = Color.WHITE;

    private static Mode mode = Mode.LIGHT;

    // Page and surfaces
    public static Color BG, SURFACE, SURFACE_ALT, INPUT_BG, LINE;
    // Text
    public static Color TEXT, MUTED, ON_PRIMARY;
    // Brand
    public static Color PRIMARY, PRIMARY_HOVER, PRIMARY_PRESSED, PRIMARY_SOFT, ACCENT;
    // Header, navigation, footer
    public static Color HEADER_BG, HEADER_TEXT, HEADER_MUTED, HEADER_LINE,
            NAV_HOVER, NAV_ACTIVE_BG, NAV_ACTIVE_TEXT, FOOTER_BG, FOOTER_TEXT;
    // Record status and warnings
    public static Color PENDING, PENDING_BG, RESOLVED, RESOLVED_BG, DANGER, DANGER_BG, WARNING, WARNING_BG;
    // Table header
    public static Color TABLE_HEAD_BG, TABLE_HEAD_TEXT;

    private static final String FAMILY = pickFamily();
    public static final Font TITLE      = new Font(FAMILY, Font.BOLD, 38);
    public static final Font TITLE_SM   = new Font(FAMILY, Font.BOLD, 28);
    public static final Font HEADING    = new Font(FAMILY, Font.BOLD, 22);
    public static final Font SUBHEAD    = new Font(FAMILY, Font.BOLD, 17);
    public static final Font STAT       = new Font(FAMILY, Font.BOLD, 28);
    public static final Font BODY       = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font BODY_BOLD  = new Font(FAMILY, Font.BOLD, 14);
    public static final Font SMALL      = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font SMALL_BOLD = new Font(FAMILY, Font.BOLD, 12);
    public static final Font CAPTION    = new Font(FAMILY, Font.BOLD, 11);
    public static final Font RECORD_ID  = new Font(FAMILY, Font.BOLD, 40);

    static {
        apply(Mode.LIGHT);
    }

    private Theme() { }

    public static Mode getMode() { return mode; }

    public static boolean isDark() { return mode == Mode.DARK; }

    /** Swaps every palette color. Build (or rebuild) screens after calling this. */
    public static void apply(Mode newMode) {
        mode = newMode;
        if (newMode == Mode.DARK) {
            BG = new Color(0x111215);
            SURFACE = new Color(0x1B1D21);
            SURFACE_ALT = new Color(0x202227);
            INPUT_BG = new Color(0x15171A);
            LINE = new Color(0x2E3137);
            TEXT = new Color(0xECECEE);
            MUTED = new Color(0x9A9DA5);
            ON_PRIMARY = WHITE;
            PRIMARY = new Color(0xE0283A);
            PRIMARY_HOVER = new Color(0xEE4352);
            PRIMARY_PRESSED = new Color(0xB31B2B);
            PRIMARY_SOFT = new Color(0x3A1A1F);
            ACCENT = new Color(0xB9BCC3);
            HEADER_BG = new Color(0x17181B);
            HEADER_TEXT = new Color(0xF2F2F3);
            HEADER_MUTED = new Color(0x9A9DA5);
            HEADER_LINE = PRIMARY;
            NAV_HOVER = new Color(0x26282D);
            NAV_ACTIVE_BG = PRIMARY;
            NAV_ACTIVE_TEXT = WHITE;
            FOOTER_BG = new Color(0x0C0D0F);
            FOOTER_TEXT = new Color(0x8D9098);
            PENDING = new Color(0xF2B347);
            PENDING_BG = new Color(0x382C12);
            RESOLVED = new Color(0x4ADE80);
            RESOLVED_BG = new Color(0x12301F);
            DANGER = new Color(0xFF6B6B);
            DANGER_BG = new Color(0x3A1517);
            WARNING = new Color(0xF2B347);
            WARNING_BG = new Color(0x382C12);
            TABLE_HEAD_BG = new Color(0x24262B);
            TABLE_HEAD_TEXT = new Color(0xC9CBD1);
        } else {
            BG = new Color(0xF4F4F5);
            SURFACE = WHITE;
            SURFACE_ALT = new Color(0xFAF6F6);
            INPUT_BG = WHITE;
            LINE = new Color(0xE2DFE0);
            TEXT = new Color(0x1E1F22);
            MUTED = new Color(0x666971);
            ON_PRIMARY = WHITE;
            PRIMARY = BSU_RED;
            PRIMARY_HOVER = new Color(0xA90D26);
            PRIMARY_PRESSED = new Color(0x8A0A1F);
            PRIMARY_SOFT = new Color(0xFCEBEE);
            ACCENT = new Color(0x55585F);
            HEADER_BG = BSU_RED;
            HEADER_TEXT = WHITE;
            HEADER_MUTED = new Color(0xF6CCD3);
            HEADER_LINE = new Color(0x8A0A1F);
            NAV_HOVER = new Color(0xA90D26);
            NAV_ACTIVE_BG = WHITE;
            NAV_ACTIVE_TEXT = BSU_RED;
            FOOTER_BG = new Color(0x2A2B2F);
            FOOTER_TEXT = new Color(0xC4C6CB);
            PENDING = new Color(0xA15C07);
            PENDING_BG = new Color(0xFDF0D5);
            RESOLVED = new Color(0x18794E);
            RESOLVED_BG = new Color(0xDDF5E7);
            DANGER = new Color(0xB4231B);
            DANGER_BG = new Color(0xFDE8E7);
            WARNING = new Color(0xA15C07);
            WARNING_BG = new Color(0xFDF0D5);
            TABLE_HEAD_BG = new Color(0x2A2B2F);
            TABLE_HEAD_TEXT = WHITE;
        }
        installDefaults();
    }

    private static void installDefaults() {
        UIManager.put("Panel.background", BG);
        UIManager.put("ToolTip.background", TEXT);
        UIManager.put("ToolTip.foreground", SURFACE);
        UIManager.put("ToolTip.font", SMALL);
        UIManager.put("ToolTip.border", new EmptyBorder(5, 8, 5, 8));
        UIManager.put("PopupMenu.background", SURFACE);
        UIManager.put("PopupMenu.border", new LineBorder(LINE, 1));
        UIManager.put("MenuItem.background", SURFACE);
        UIManager.put("MenuItem.foreground", TEXT);
        UIManager.put("MenuItem.selectionBackground", PRIMARY_SOFT);
        UIManager.put("MenuItem.selectionForeground", PRIMARY);
        UIManager.put("MenuItem.font", BODY_BOLD);
        UIManager.put("ComboBox.background", INPUT_BG);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_SOFT);
        UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("ComboBox.font", BODY);
        UIManager.put("List.background", INPUT_BG);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", PRIMARY_SOFT);
        UIManager.put("List.selectionForeground", TEXT);
        UIManager.put("ComboBox.buttonBackground", INPUT_BG);
        UIManager.put("ComboBox.buttonDarkShadow", MUTED);
        UIManager.put("ComboBox.buttonShadow", INPUT_BG);
        UIManager.put("ComboBox.buttonHighlight", INPUT_BG);
        UIManager.put("Separator.foreground", LINE);
        UIManager.put("Separator.background", SURFACE);
        UIManager.put("PopupMenuSeparator.foreground", LINE);
        UIManager.put("PopupMenuSeparator.background", SURFACE);
        UIManager.put("MenuItem.disabledForeground", MUTED);
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("Table.scrollPaneBorder", BorderFactory.createEmptyBorder());
    }

    public static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default look and feel is fine if this fails.
        }
        installDefaults();
    }

    private static String pickFamily() {
        try {
            Set<String> installed = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String f : new String[]{"Segoe UI", "Helvetica Neue", "Inter", "Open Sans", "Roboto", "DejaVu Sans"}) {
                if (installed.contains(f)) {
                    return f;
                }
            }
        } catch (HeadlessException ignored) {
            // Fall through to the logical font.
        }
        return Font.SANS_SERIF;
    }

    // ---------------------------------------------------------------- helpers

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    /** Multi-line text that wraps to whatever width its container gives it. */
    public static WrapText wrapped(String text, Font font, Color color) {
        return new WrapText(text, font, color);
    }

    public static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    public static void fixHeight(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }

    /**
     * Transparent panel whose maximum height equals its preferred height, so
     * vertical stacks keep tidy spacing instead of stretching their rows.
     */
    public static JPanel transparent(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                return isMaximumSizeSet() ? max : new Dimension(max.width, getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        return p;
    }

    /** Transparent vertical stack (BoxLayout) that doesn't stretch taller than its content. */
    public static JPanel vstack() {
        JPanel p = transparent(null);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static void styleInput(JTextComponent c) {
        c.setFont(BODY);
        c.setForeground(TEXT);
        c.setCaretColor(PRIMARY);
        c.setBackground(INPUT_BG);
        c.setSelectionColor(PRIMARY_SOFT);
        c.setSelectedTextColor(TEXT);
        c.setDisabledTextColor(MUTED);
        setInputBorder(c, LINE);
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { setInputBorder(c, PRIMARY); }
            @Override public void focusLost(FocusEvent e) { setInputBorder(c, LINE); }
        });
    }

    private static void setInputBorder(JComponent c, Color color) {
        c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(10, 12, 10, 12)));
    }

    public static void styleScroll(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setOpaque(false);
    }

    /** A small uppercase caption such as "SR-CODE" above a value. */
    public static JLabel caption(String text) {
        return label(text.toUpperCase(), CAPTION, MUTED);
    }

    /** App mark: a red rounded square holding a white speech bubble. */
    public static BufferedImage logo(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float u = s / 10f;
        g.setColor(BSU_RED);
        g.fill(new RoundRectangle2D.Float(0, 0, s, s, s / 3.2f, s / 3.2f));
        g.setColor(WHITE);
        g.fill(new RoundRectangle2D.Float(1.9f * u, 2.2f * u, 6.2f * u, 4.4f * u, 2.0f * u, 2.0f * u));
        Polygon tail = new Polygon();
        tail.addPoint(Math.round(3.2f * u), Math.round(6.2f * u));
        tail.addPoint(Math.round(2.9f * u), Math.round(8.1f * u));
        tail.addPoint(Math.round(4.9f * u), Math.round(6.4f * u));
        g.fill(tail);
        g.setColor(BSU_RED);
        float d = 0.9f * u;
        for (int i = 0; i < 3; i++) {
            g.fill(new Ellipse2D.Float((3.2f + i * 1.35f) * u, 3.95f * u, d, d));
        }
        g.dispose();
        return img;
    }

    /** Fills a pill-shaped status badge behind a label. */
    public static void paintBadge(Graphics2D g2, int x, int y, int w, int h, Color bg) {
        g2.setColor(bg);
        g2.fillRoundRect(x, y, w, h, h, h);
    }

    private static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int orientation) { return invisibleButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return invisibleButton(); }

        private JButton invisibleButton() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            return b;
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // Transparent track keeps the page clean.
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? MUTED : LINE);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }
}
