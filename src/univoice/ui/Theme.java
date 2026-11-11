package univoice.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public final class Theme {

    public static final Color ESPRESSO   = new Color(0x2B1B14);
    public static final Color DARK_ROAST = new Color(0x4A3228);
    public static final Color MOCHA      = new Color(0x6F4E37);
    public static final Color CARAMEL    = new Color(0xB07A45);
    public static final Color LATTE      = new Color(0xDCC3A2);
    public static final Color CREAM      = new Color(0xF4EADB);
    public static final Color FOAM       = new Color(0xFCF8F1);
    public static final Color INPUT_BG   = new Color(0xFFFCF7);
    public static final Color LINE       = new Color(0xE3D3BC);
    public static final Color TEXT       = new Color(0x3B2A21);
    public static final Color MUTED      = new Color(0x8A7262);
    public static final Color PENDING    = new Color(0x9C6A1E);
    public static final Color PENDING_BG = new Color(0xF5E1BC);
    public static final Color RESOLVED   = new Color(0x4F6F40);
    public static final Color RESOLVED_BG = new Color(0xDCE7D0);
    public static final Color DANGER     = new Color(0xA3402B);

    private static final String SERIF = "Georgia";
    public static final Font TITLE     = new Font(SERIF, Font.BOLD, 42);
    public static final Font HEADING   = new Font(SERIF, Font.BOLD, 24);
    public static final Font SUBHEAD   = new Font(SERIF, Font.BOLD, 18);
    public static final Font STAT      = new Font(SERIF, Font.BOLD, 28);
    public static final Font BODY      = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font SMALL     = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    public static final Font SMALL_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    private Theme() { }

    public static void install() {
        try {

            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {

        }
        UIManager.put("Panel.background", FOAM);
        UIManager.put("OptionPane.background", FOAM);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY);
        UIManager.put("ToolTip.background", ESPRESSO);
        UIManager.put("ToolTip.foreground", FOAM);
        UIManager.put("ToolTip.font", SMALL);
        UIManager.put("ToolTip.border", new EmptyBorder(5, 8, 5, 8));
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JTextArea wrapped(String text, Font font, Color color, int width) {
        JTextArea a = new JTextArea(text);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setBorder(null);
        a.setFont(font);
        a.setForeground(color);
        a.setSize(width, Short.MAX_VALUE);
        Dimension d = new Dimension(width, a.getPreferredSize().height);
        a.setPreferredSize(d);
        a.setMaximumSize(d);
        return a;
    }

    public static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    public static void fixHeight(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
    }

    public static void styleInput(JTextComponent c) {
        c.setFont(BODY);
        c.setForeground(TEXT);
        c.setCaretColor(MOCHA);
        c.setBackground(INPUT_BG);
        setInputBorder(c, LINE);
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { setInputBorder(c, CARAMEL); }
            @Override public void focusLost(FocusEvent e) { setInputBorder(c, LINE); }
        });
    }

    private static void setInputBorder(JComponent c, Color color) {
        c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(9, 12, 9, 12)));
    }

    public static void styleScroll(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new CoffeeScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new CoffeeScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        sp.getVerticalScrollBar().setUnitIncrement(16);
    }

    public static BufferedImage logo(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(CARAMEL);
        g.fill(new RoundRectangle2D.Float(0, 0, s, s, s / 3f, s / 3f));
        float u = s / 10f;
        g.setColor(CREAM);
        g.fill(new RoundRectangle2D.Float(2.4f * u, 4.6f * u, 4.4f * u, 3.2f * u, u, u));
        g.fill(new RoundRectangle2D.Float(1.9f * u, 8.1f * u, 5.4f * u, 0.5f * u, 0.5f * u, 0.5f * u));
        g.setStroke(new BasicStroke(0.55f * u, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(5.9f * u, 5.2f * u, 1.9f * u, 1.9f * u, -90, 180, Arc2D.OPEN));
        g.draw(new QuadCurve2D.Float(3.6f * u, 3.9f * u, 3.0f * u, 3.2f * u, 3.6f * u, 2.5f * u));
        g.draw(new QuadCurve2D.Float(5.4f * u, 3.9f * u, 4.8f * u, 3.2f * u, 5.4f * u, 2.5f * u));
        g.dispose();
        return img;
    }

    private static class CoffeeScrollBarUI extends BasicScrollBarUI {
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
            g.setColor(CREAM);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? CARAMEL : LATTE);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }
}
