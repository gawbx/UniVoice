package univoice.ui;

import javax.swing.*;
import java.awt.*;

/** Card with rounded corners, a hairline border, and an optional colored top strip. */
public class RoundedPanel extends JPanel {

    private final int arc;
    private Color borderColor;
    private Color accent;

    public RoundedPanel(int arc, Color fill, Color borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        setBackground(fill);
        setOpaque(false);
    }

    /** Standard white (or dark) card used across the app. */
    public static RoundedPanel card() {
        return new RoundedPanel(18, Theme.SURFACE, Theme.LINE);
    }

    /** Paints a thin brand-colored strip along the top edge. */
    public RoundedPanel withAccent(Color accent) {
        this.accent = accent;
        return this;
    }

    public void setBorderColor(Color c) {
        this.borderColor = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 1, h = getHeight() - 1;
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w, h, arc, arc);
        if (accent != null) {
            Shape old = g2.getClip();
            g2.clipRect(0, 0, getWidth(), 4);
            g2.setColor(accent);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setClip(old);
        }
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w, h, arc, arc);
        }
        g2.dispose();
    }
}
