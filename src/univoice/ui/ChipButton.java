package univoice.ui;

import javax.swing.*;
import java.awt.*;

public class ChipButton extends JToggleButton {

    public ChipButton(String text) {
        super(text);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean selected = isSelected();
        boolean hover = getModel().isRollover();
        int w = getWidth(), h = getHeight();

        if (selected) {
            g2.setColor(Theme.MOCHA);
            g2.fillRoundRect(0, 0, w, h, h, h);
        } else {
            g2.setColor(Theme.INPUT_BG);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(hover ? Theme.CARAMEL : Theme.LINE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
        }

        g2.setFont(getFont());
        g2.setColor(selected ? Theme.FOAM : Theme.TEXT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
