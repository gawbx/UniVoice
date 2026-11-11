package univoice.ui;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {

    private final int arc;
    private final Color borderColor;
    private int fixedWidth = -1;

    public RoundedPanel(int arc, Color fill, Color borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        setBackground(fill);
        setOpaque(false);
    }

    public void setFixedWidth(int width) {
        this.fixedWidth = width;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return fixedWidth > 0 ? new Dimension(fixedWidth, d.height) : d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        }
        g2.dispose();
    }
}
