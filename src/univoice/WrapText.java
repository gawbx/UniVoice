package univoice.ui;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Read-only text that wraps to the width its container gives it, so long
 * sentences reflow when the window gets narrower (for example in mobile view).
 */
public class WrapText extends JTextArea {

    private static final int HINT_WIDTH = 280;
    private int lastWidth = -1;

    public WrapText(String text, Font font, Color color) {
        super(text);
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(null);
        setFont(font);
        setForeground(color);
        setHighlighter(null);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (getWidth() != lastWidth) {
                    lastWidth = getWidth();
                    SwingUtilities.invokeLater(() -> {
                        revalidate();
                        repaint();
                    });
                }
            }
        });
    }

    private int heightFor(int width) {
        Insets in = getInsets();
        View root = getUI().getRootView(this);
        root.setSize(Math.max(1, width - in.left - in.right), Integer.MAX_VALUE);
        return (int) Math.ceil(root.getPreferredSpan(View.Y_AXIS)) + in.top + in.bottom;
    }

    @Override
    public Dimension getPreferredSize() {
        int w = getWidth() > 0 ? getWidth() : HINT_WIDTH;
        return new Dimension(Math.min(w, HINT_WIDTH), heightFor(w));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(60, getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
