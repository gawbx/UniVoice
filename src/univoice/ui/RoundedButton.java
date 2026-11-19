package univoice.ui;

import javax.swing.*;
import java.awt.*;

/** Pill-shaped button painted with the current theme colors. */
public class RoundedButton extends JButton {

    public enum Style {
        /** Main action, filled with BatStateU red. */
        PRIMARY,
        /** Neutral outline button. */
        SECONDARY,
        /** Soft red outline for risky actions that will ask for confirmation. */
        DANGER,
        /** Solid red for the final "yes, do it" in a warning dialog. */
        DANGER_SOLID,
        /** Header navigation item. */
        NAV,
        /** Header button with an outline, used for "Log out". */
        NAV_OUTLINE
    }

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
        boolean nav = style == Style.NAV || style == Style.NAV_OUTLINE;
        setBorder(BorderFactory.createEmptyBorder(nav ? 8 : 11, nav ? 16 : 22, nav ? 8 : 11, nav ? 16 : 22));
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    /** Lets the button stretch to the full width of a vertical stack. */
    public RoundedButton fullWidth() {
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        return this;
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
                fill = !on ? Theme.LINE : pressed ? Theme.PRIMARY_PRESSED : hover ? Theme.PRIMARY_HOVER : Theme.PRIMARY;
                text = on ? Theme.ON_PRIMARY : Theme.MUTED;
                break;
            case DANGER_SOLID:
                fill = !on ? Theme.LINE : pressed ? Theme.PRIMARY_PRESSED : hover ? Theme.PRIMARY_HOVER : Theme.DANGER;
                text = on ? Color.WHITE : Theme.MUTED;
                break;
            case DANGER:
                fill = !on ? null : hover || pressed ? Theme.DANGER_BG : null;
                border = on ? Theme.DANGER : Theme.LINE;
                text = on ? Theme.DANGER : Theme.MUTED;
                break;
            case NAV:
                fill = active ? Theme.NAV_ACTIVE_BG : hover ? Theme.NAV_HOVER : null;
                text = active ? Theme.NAV_ACTIVE_TEXT : Theme.HEADER_TEXT;
                break;
            case NAV_OUTLINE:
                fill = hover ? Theme.NAV_HOVER : null;
                border = Theme.HEADER_MUTED;
                text = Theme.HEADER_TEXT;
                break;
            default:
                fill = hover || pressed ? Theme.SURFACE_ALT : Theme.SURFACE;
                border = !on ? Theme.LINE : hover ? Theme.MUTED : Theme.LINE;
                text = on ? Theme.TEXT : Theme.MUTED;
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
