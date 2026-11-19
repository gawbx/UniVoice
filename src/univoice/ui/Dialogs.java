package univoice.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;

import static univoice.ui.Theme.*;

/**
 * Themed pop-ups for warnings and confirmations. Every action that logs out,
 * deletes, changes a record, or exits goes through one of these first.
 */
public final class Dialogs {

    public enum Kind { INFO, WARNING, DANGER, SUCCESS }

    private Dialogs() { }

    /** Asks a yes/no question. Returns true only when the user clicks the confirm button. */
    public static boolean confirm(Component parent, Kind kind, String title, String message,
                                  String confirmText, String cancelText) {
        Holder<Boolean> result = new Holder<>(false);
        JDialog d = create(parent);
        RoundedButton ok = new RoundedButton(confirmText,
                kind == Kind.DANGER ? RoundedButton.Style.DANGER_SOLID : RoundedButton.Style.PRIMARY);
        RoundedButton cancel = new RoundedButton(cancelText, RoundedButton.Style.SECONDARY);
        ok.addActionListener(e -> { result.value = true; d.dispose(); });
        cancel.addActionListener(e -> d.dispose());
        show(d, parent, kind, title, message, null, ok, cancel);
        return result.value;
    }

    /** Shows a message with a single button. */
    public static void info(Component parent, Kind kind, String title, String message) {
        JDialog d = create(parent);
        RoundedButton ok = new RoundedButton("OK", RoundedButton.Style.PRIMARY);
        ok.addActionListener(e -> d.dispose());
        show(d, parent, kind, title, message, null, ok, null);
    }

    /**
     * Strong confirmation for removing a record that may still be pending.
     * The admin must pick a reason and type the record ID before the button unlocks.
     *
     * @return the chosen reason, or null if the admin cancelled
     */
    public static String confirmRemoval(Component parent, String title, String message,
                                        String mustType, String[] reasons, String confirmText) {
        Holder<String> result = new Holder<>(null);
        JDialog d = create(parent);

        JPanel extra = vstack();
        extra.add(left(caption("Reason")));
        extra.add(Box.createVerticalStrut(6));
        JComboBox<String> reasonBox = new JComboBox<>(reasons);
        reasonBox.setFont(BODY);
        reasonBox.setBackground(INPUT_BG);
        reasonBox.setForeground(TEXT);
        reasonBox.setBorder(new LineBorder(LINE, 1, true));
        reasonBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        reasonBox.setPreferredSize(new Dimension(200, 38));
        extra.add(left(reasonBox));
        extra.add(Box.createVerticalStrut(14));

        JLabel typeHint = label("<html>Type <b>" + mustType + "</b> to confirm</html>", SMALL, MUTED);
        extra.add(left(typeHint));
        extra.add(Box.createVerticalStrut(6));
        JTextField typed = new JTextField();
        styleInput(typed);
        typed.setFont(BODY_BOLD);
        fixHeight(typed);
        extra.add(left(typed));

        RoundedButton ok = new RoundedButton(confirmText, RoundedButton.Style.DANGER_SOLID);
        ok.setEnabled(false);
        RoundedButton cancel = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        typed.getDocument().addDocumentListener(new DocumentListener() {
            private void check() { ok.setEnabled(typed.getText().trim().equalsIgnoreCase(mustType)); }
            @Override public void insertUpdate(DocumentEvent e) { check(); }
            @Override public void removeUpdate(DocumentEvent e) { check(); }
            @Override public void changedUpdate(DocumentEvent e) { check(); }
        });
        ok.addActionListener(e -> {
            if (ok.isEnabled()) {
                result.value = String.valueOf(reasonBox.getSelectedItem());
                d.dispose();
            }
        });
        typed.addActionListener(e -> ok.doClick());
        cancel.addActionListener(e -> d.dispose());
        SwingUtilities.invokeLater(typed::requestFocusInWindow);
        show(d, parent, Kind.DANGER, title, message, extra, ok, cancel);
        return result.value;
    }

    // ------------------------------------------------------------ internals

    private static final class Holder<T> {
        T value;
        Holder(T value) { this.value = value; }
    }

    private static JDialog create(Component parent) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        if (owner == null && parent instanceof Window) {
            owner = (Window) parent;
        }
        JDialog d = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        return d;
    }

    private static void show(JDialog d, Component parent, Kind kind, String title, String message,
                             JComponent extra, RoundedButton ok, RoundedButton cancel) {
        Window owner = d.getOwner();
        int ownerW = owner != null ? owner.getWidth() : 800;
        int width = Math.max(290, Math.min(440, ownerW - 32));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);
        root.setBorder(new CompoundBorder(new LineBorder(isDark() ? LINE : new Color(0xCFCBCC), 1),
                new EmptyBorder(22, 22, 18, 22)));

        JPanel body = vstack();
        JPanel head = transparent(new BorderLayout(12, 0));
        head.add(new KindIcon(kind), BorderLayout.WEST);
        JLabel t = label(title, SUBHEAD, TEXT);
        head.add(t, BorderLayout.CENTER);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        body.add(head);
        body.add(Box.createVerticalStrut(12));

        WrapText msg = wrapped(message, BODY, MUTED);
        msg.setSize(width - 46, 10);
        body.add(msg);
        if (extra != null) {
            body.add(Box.createVerticalStrut(16));
            extra.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(extra);
        }
        body.add(Box.createVerticalStrut(20));

        JPanel buttons = transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (cancel != null) {
            buttons.add(cancel);
        }
        buttons.add(ok);
        fixHeight(buttons);
        body.add(buttons);
        root.add(body, BorderLayout.CENTER);

        d.setContentPane(root);
        d.getRootPane().setDefaultButton(cancel == null || extra == null ? ok : null);
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        root.setSize(width, 10);
        Page.layoutDeep(root);
        Page.layoutDeep(root);
        d.setSize(width, root.getPreferredSize().height);
        d.setLocationRelativeTo(owner != null ? owner : parent);
        if (extra == null) {
            SwingUtilities.invokeLater((cancel != null ? cancel : ok)::requestFocusInWindow);
        }
        d.setVisible(true);
    }

    /** Round icon at the top-left of each dialog. */
    private static final class KindIcon extends JComponent {
        private final Kind kind;

        KindIcon(Kind kind) {
            this.kind = kind;
            setPreferredSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            Color fg, bg;
            switch (kind) {
                case DANGER: fg = DANGER; bg = DANGER_BG; break;
                case WARNING: fg = WARNING; bg = WARNING_BG; break;
                case SUCCESS: fg = RESOLVED; bg = RESOLVED_BG; break;
                default: fg = PRIMARY; bg = PRIMARY_SOFT; break;
            }
            int s = Math.min(getWidth(), getHeight());
            int y0 = (getHeight() - s) / 2;
            g2.setColor(bg);
            g2.fillOval(0, y0, s, s);
            g2.setColor(fg);
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = s / 2, cy = y0 + s / 2;
            if (kind == Kind.SUCCESS) {
                g2.drawPolyline(new int[]{cx - 7, cx - 2, cx + 7}, new int[]{cy, cy + 5, cy - 5}, 3);
            } else if (kind == Kind.INFO) {
                g2.fillOval(cx - 2, cy - 9, 4, 4);
                g2.drawLine(cx, cy - 1, cx, cy + 8);
            } else {
                g2.drawLine(cx, cy - 8, cx, cy + 2);
                g2.fillOval(cx - 2, cy + 6, 4, 4);
            }
            g2.dispose();
        }
    }
}
