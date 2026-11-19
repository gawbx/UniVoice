package univoice.ui;

import java.awt.*;

/**
 * A responsive grid. It fits as many equal columns as the width allows
 * (never fewer than one, never more than {@code maxCols}), so the same set of
 * cards shows side by side on a laptop and stacked on a phone-sized window.
 * Cards in the same row are stretched to the same height for a tidy look.
 */
public class GridFlowLayout implements LayoutManager {

    private final int minColWidth;
    private int maxCols;
    private final int hgap;
    private final int vgap;

    public GridFlowLayout(int minColWidth, int maxCols, int hgap, int vgap) {
        this.minColWidth = minColWidth;
        this.maxCols = maxCols;
        this.hgap = hgap;
        this.vgap = vgap;
    }

    public void setMaxCols(int maxCols) {
        this.maxCols = Math.max(1, maxCols);
    }

    private int columnsFor(int width, int count) {
        int cols = (width + hgap) / (minColWidth + hgap);
        cols = Math.max(1, Math.min(cols, maxCols));
        return Math.max(1, Math.min(cols, Math.max(1, count)));
    }

    private int innerWidth(Container parent) {
        Insets in = parent.getInsets();
        int w = parent.getWidth();
        if (w <= 0) {
            w = maxCols * minColWidth + (maxCols - 1) * hgap + in.left + in.right;
        }
        return w - in.left - in.right;
    }

    /** Lays out (or only measures, when {@code apply} is false) and returns the total height. */
    private int arrange(Container parent, boolean apply) {
        Insets in = parent.getInsets();
        int width = innerWidth(parent);
        int count = 0;
        for (Component c : parent.getComponents()) {
            if (c.isVisible()) {
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        int cols = columnsFor(width, count);
        int colW = Math.max(1, (width - (cols - 1) * hgap) / cols);

        Component[] visible = new Component[count];
        int k = 0;
        for (Component c : parent.getComponents()) {
            if (c.isVisible()) {
                visible[k++] = c;
            }
        }

        int y = in.top;
        for (int start = 0; start < count; start += cols) {
            int end = Math.min(count, start + cols);
            int rowH = 0;
            for (int i = start; i < end; i++) {
                Component c = visible[i];
                c.setSize(colW, Math.max(1, c.getHeight()));
                if (c instanceof Container) {
                    Page.layoutDeep((Container) c);
                }
                rowH = Math.max(rowH, c.getPreferredSize().height);
            }
            if (apply) {
                int x = in.left;
                for (int i = start; i < end; i++) {
                    visible[i].setBounds(x, y, colW, rowH);
                    x += colW + hgap;
                }
            }
            y += rowH + (end < count ? vgap : 0);
        }
        return y - in.top;
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Insets in = parent.getInsets();
        int h = arrange(parent, false);
        int w = parent.getWidth() > 0 ? parent.getWidth() : innerWidth(parent) + in.left + in.right;
        return new Dimension(Math.min(w, minColWidth + in.left + in.right), h + in.top + in.bottom);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Insets in = parent.getInsets();
        return new Dimension(Math.min(minColWidth, 200) + in.left + in.right, in.top + in.bottom);
    }

    @Override
    public void layoutContainer(Container parent) {
        arrange(parent, true);
    }

    @Override public void addLayoutComponent(String name, Component comp) { }
    @Override public void removeLayoutComponent(Component comp) { }
}
