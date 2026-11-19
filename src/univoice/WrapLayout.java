package univoice.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A left-aligned FlowLayout that moves items to the next line when the row
 * is full and reports the correct height, so chip rows wrap on small screens.
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout(int hgap, int vgap) {
        super(FlowLayout.LEFT, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension d = layoutSize(target, false);
        d.width -= getHgap() + 1;
        return d;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getWidth();
            if (targetWidth == 0) {
                targetWidth = 420;
            }
            Insets insets = target.getInsets();
            int hgap = getHgap();
            int vgap = getVgap();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;
            for (Component m : target.getComponents()) {
                if (!m.isVisible()) {
                    continue;
                }
                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                if (rowWidth + d.width > maxWidth) {
                    addRow(dim, rowWidth, rowHeight);
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth != 0) {
                    rowWidth += hgap;
                }
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            addRow(dim, rowWidth, rowHeight);
            dim.width += insets.left + insets.right + hgap * 2;
            dim.height += insets.top + insets.bottom + vgap * 2;
            Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scroll != null && target.isValid()) {
                dim.width -= (hgap + 1);
            }
            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += getVgap();
        }
        dim.height += rowHeight;
    }

    /** A transparent panel that uses this layout with no outer padding. */
    public static JPanel row(int hgap, int vgap) {
        JPanel p = new JPanel(new WrapLayout(hgap, vgap)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(-vgap, -hgap, -vgap, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }
}
