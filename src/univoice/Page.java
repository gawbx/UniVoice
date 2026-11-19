package univoice.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A scrollable page that centers one content column. The column is never
 * wider than {@code maxWidth} and shrinks with the window, which is what makes
 * the screens usable in mobile view.
 */
public class Page extends JPanel implements Scrollable {

    private final JComponent content;
    private final int maxWidth;
    private final boolean centerVertically;
    private int padX;
    private int padY;

    public Page(JComponent content, int maxWidth, boolean centerVertically) {
        this.content = content;
        this.maxWidth = maxWidth;
        this.centerVertically = centerVertically;
        setOpaque(true);
        setBackground(Theme.BG);
        setLayout(null);
        add(content);
        setPadding(32, 28);
    }

    public final void setPadding(int padX, int padY) {
        this.padX = padX;
        this.padY = padY;
        revalidate();
        repaint();
    }

    /** Lays out a container and all its children top-down, without touching validity flags. */
    static void layoutDeep(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) {
                layoutDeep((Container) child);
            }
        }
    }

    private int columnWidth() {
        int avail = (getWidth() > 0 ? getWidth() : maxWidth + 2 * padX) - 2 * padX;
        return Math.max(200, Math.min(maxWidth, avail));
    }

    private int contentHeight(int width) {
        content.setSize(width, Math.max(1, content.getHeight()));
        layoutDeep(content);
        layoutDeep(content);
        return content.getPreferredSize().height;
    }

    @Override
    public void doLayout() {
        int w = columnWidth();
        int h = contentHeight(w);
        int x = Math.max(0, (getWidth() - w) / 2);
        int y = padY;
        if (centerVertically && getHeight() > h + 2 * padY) {
            y = (getHeight() - h) / 2;
        }
        content.setBounds(x, y, w, h);
        layoutDeep(content);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = columnWidth();
        return new Dimension(w + 2 * padX, contentHeight(w) + 2 * padY);
    }

    /** Wraps this page in a borderless, themed scroll pane. */
    public JScrollPane inScroll() {
        JScrollPane sp = new JScrollPane(this);
        sp.setBorder(null);
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Theme.BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Theme.styleScroll(sp);
        return sp;
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 18; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return Math.max(18, r.height - 40); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container p = getParent();
        return p instanceof JViewport && p.getHeight() > getPreferredSize().height;
    }
}
