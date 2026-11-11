package univoice.ui;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, DANGER, NAV }

    private final Style style;
    private boolean active;

    public RoundedButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.BODY_BOLD);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean on = isEnabled();
        boolean hover = on && getModel().isRollover();
        boolean pressed = on && getModel().isArmed();

        Color fill = null;
        Color border = null;
        Color text;

        switch (style) {
            case PRIMARY:
                fill = !on ? Theme.LATTE : pressed ? Theme.ESPRESSO : hover ? Theme.DARK_ROAST : Theme.MOCHA;
                text = on ? Theme.FOAM : Theme.MUTED;
                break;
            case DANGER:
                fill = !on ? Theme.LATTE : pressed ? new Color(0x7A2E1D) : hover ? new Color(0x8C3623) : Theme.DANGER;
                text = on ? Theme.FOAM : Theme.MUTED;
                break;
            case NAV:
                fill = active ? Theme.MOCHA : hover ? Theme.DARK_ROAST : null;
                text = active ? Theme.FOAM : Theme.LATTE;
                break;
            default:
                fill = hover || pressed ? Theme.LATTE : null;
                border = on ? Theme.MOCHA : Theme.LINE;
                text = on ? Theme.MOCHA : Theme.MUTED;
                break;
        }

        int w = getWidth(), h = getHeight();
        if (fill != null) {
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, h, h);
        }
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }

        g2.setFont(getFont());
        g2.setColor(text);
        FontMetrics fm = g2.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
