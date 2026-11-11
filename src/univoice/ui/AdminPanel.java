package univoice.ui;

import univoice.model.Feedback;
import univoice.model.Status;
import univoice.service.FeedbackManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static univoice.ui.Theme.*;

public class AdminPanel extends JPanel {

    private static final String[] COLUMNS = {"Record ID", "SR-Code", "Dept", "Type", "Status", "Filed", "Message"};
    private static final Color ZEBRA = new Color(0xF7EEE1);

    private final FeedbackManager manager;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField();
    private final JTextArea details = new JTextArea();
    private final JLabel totalValue = label("0", STAT, ESPRESSO);
    private final JLabel pendingValue = label("0", STAT, PENDING);
    private final JLabel resolvedValue = label("0", STAT, RESOLVED);
    private final RoundedButton resolveBtn = new RoundedButton("Mark as Resolved", RoundedButton.Style.PRIMARY);
    private final RoundedButton pendingBtn = new RoundedButton("Mark as Pending", RoundedButton.Style.SECONDARY);
    private final RoundedButton deleteBtn = new RoundedButton("Delete record", RoundedButton.Style.DANGER);

    private List<Feedback> shown = new ArrayList<>();
    private Status filter = null;

    public AdminPanel(FeedbackManager manager, Runnable onLogout) {
        this.manager = manager;
        setLayout(new BorderLayout(0, 16));
        setBackground(CREAM);
        setBorder(new EmptyBorder(24, 32, 20, 32));

        add(buildTop(onLogout), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildDetailsCard(), BorderLayout.EAST);

        resolveBtn.addActionListener(e -> changeStatus(Status.RESOLVED));
        pendingBtn.addActionListener(e -> changeStatus(Status.PENDING));
        deleteBtn.addActionListener(e -> deleteSelected());
        refresh();
    }

    private JPanel buildTop(Runnable onLogout) {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(left(label("Admin dashboard", HEADING, ESPRESSO)));
        titles.add(left(label("Review, resolve, and clean up campus feedback.", SMALL, MUTED)));
        RoundedButton logout = new RoundedButton("Log out", RoundedButton.Style.SECONDARY);
        logout.addActionListener(e -> onLogout.run());
        JPanel logoutWrap = new JPanel(new GridBagLayout());
        logoutWrap.setOpaque(false);
        logoutWrap.add(logout);
        titleRow.add(titles, BorderLayout.WEST);
        titleRow.add(logoutWrap, BorderLayout.EAST);
        top.add(left(titleRow));
        top.add(Box.createVerticalStrut(14));

        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard(totalValue, "TOTAL RECORDS"));
        stats.add(statCard(pendingValue, "PENDING"));
        stats.add(statCard(resolvedValue, "RESOLVED"));
        top.add(left(stats));
        top.add(Box.createVerticalStrut(14));

        top.add(left(buildToolbar()));
        return top;
    }

    private RoundedPanel statCard(JLabel value, String caption) {
        RoundedPanel p = new RoundedPanel(18, FOAM, LINE);
        p.setLayout(new BorderLayout(14, 0));
        p.setBorder(new EmptyBorder(12, 20, 12, 20));
        p.add(value, BorderLayout.WEST);
        p.add(label(caption, SMALL_BOLD, MUTED), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel chips = new JPanel();
        chips.setOpaque(false);
        chips.setLayout(new BoxLayout(chips, BoxLayout.X_AXIS));
        ButtonGroup group = new ButtonGroup();
        ChipButton all = new ChipButton("All");
        ChipButton pending = new ChipButton("Pending");
        ChipButton resolved = new ChipButton("Resolved");
        for (ChipButton c : new ChipButton[]{all, pending, resolved}) {
            group.add(c);
            chips.add(c);
            chips.add(Box.createHorizontalStrut(8));
        }
        all.setSelected(true);
        all.addActionListener(e -> { filter = null; refresh(); });
        pending.addActionListener(e -> { filter = Status.PENDING; refresh(); });
        resolved.addActionListener(e -> { filter = Status.RESOLVED; refresh(); });

        styleInput(searchField);
        searchField.setPreferredSize(new Dimension(220, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        RoundedButton clear = new RoundedButton("Clear", RoundedButton.Style.SECONDARY);
        clear.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        clear.addActionListener(e -> searchField.setText(""));

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        search.setOpaque(false);
        search.add(label("Search SR-Code:", SMALL_BOLD, TEXT));
        search.add(searchField);
        search.add(clear);

        bar.add(chips, BorderLayout.WEST);
        bar.add(search, BorderLayout.EAST);
        return bar;
    }

    private RoundedPanel buildTableCard() {
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(FOAM);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        int[] widths = {84, 84, 58, 94, 96, 122, 200};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetails();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FOAM);
        styleScroll(scroll);

        RoundedPanel card = new RoundedPanel(20, FOAM, LINE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(8, 8, 8, 8));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDetailsCard() {
        RoundedPanel card = new RoundedPanel(20, FOAM, LINE);
        card.setFixedWidth(290);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        card.add(left(label("Record details", SUBHEAD, ESPRESSO)));
        card.add(Box.createVerticalStrut(10));

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(BODY);
        details.setForeground(TEXT);
        details.setOpaque(false);
        details.setBorder(null);
        JScrollPane detailsScroll = new JScrollPane(details);
        detailsScroll.setBorder(null);
        detailsScroll.setOpaque(false);
        detailsScroll.getViewport().setOpaque(false);
        styleScroll(detailsScroll);
        detailsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(detailsScroll);
        card.add(Box.createVerticalStrut(14));

        deleteBtn.setToolTipText("Only Resolved records can be deleted");
        for (RoundedButton b : new RoundedButton[]{resolveBtn, pendingBtn, deleteBtn}) {
            fixHeight(b);
            card.add(left(b));
            card.add(Box.createVerticalStrut(8));
        }

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 18, 0, 0));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    public void refresh() {
        Feedback previous = selectedRecord();
        int keepId = previous == null ? -1 : previous.getId();

        shown = manager.listRecords(filter, searchField.getText());
        model.setRowCount(0);
        int reselect = -1;
        for (int i = 0; i < shown.size(); i++) {
            Feedback f = shown.get(i);
            model.addRow(new Object[]{f.getRecordId(), f.getSrCode(), f.getDepartment(), f.getType(),
                    f.getStatus().getLabel(), f.getDateFiledText(), f.getMessage().replace('\n', ' ')});
            if (f.getId() == keepId) {
                reselect = i;
            }
        }
        totalValue.setText(String.valueOf(manager.count(null)));
        pendingValue.setText(String.valueOf(manager.count(Status.PENDING)));
        resolvedValue.setText(String.valueOf(manager.count(Status.RESOLVED)));

        if (reselect >= 0) {
            table.setRowSelectionInterval(reselect, reselect);
        } else {
            table.clearSelection();
        }
        updateDetails();
    }

    private Feedback selectedRecord() {
        int row = table.getSelectedRow();
        return (row >= 0 && row < shown.size()) ? shown.get(row) : null;
    }

    private void updateDetails() {
        Feedback f = selectedRecord();
        if (f == null) {
            details.setText("Select a record from the table to see its full details.");
            resolveBtn.setEnabled(false);
            pendingBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
            return;
        }
        details.setText(f.display());
        details.setCaretPosition(0);
        resolveBtn.setEnabled(!f.isResolved());
        pendingBtn.setEnabled(f.isResolved());
        deleteBtn.setEnabled(f.isResolved());
    }

    private void changeStatus(Status newStatus) {
        Feedback f = selectedRecord();
        if (f != null) {
            manager.updateStatus(f.getId(), newStatus);
            refresh();
        }
    }

    private void deleteSelected() {
        Feedback f = selectedRecord();
        if (f == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete record " + f.getRecordId() + "?\nThis cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.deleteRecord(f.getId());
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot delete", JOptionPane.WARNING_MESSAGE);
        }
        refresh();
    }

    private static Color rowColor(int row, boolean selected) {
        return selected ? LATTE : (row % 2 == 0 ? FOAM : ZEBRA);
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            setForeground(TEXT);
            setBackground(rowColor(row, selected));
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(SMALL_BOLD);
            setForeground(FOAM);
            setBackground(DARK_ROAST);
            return this;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        private Status status = Status.PENDING;
        private Color rowBg = FOAM;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            status = Status.fromString(String.valueOf(value));
            rowBg = rowColor(row, selected);
            setOpaque(false);
            setHorizontalAlignment(CENTER);
            setFont(SMALL_BOLD);
            setBorder(null);
            setForeground(status == Status.RESOLVED ? RESOLVED : PENDING);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int w = fm.stringWidth(getText()) + 26;
            int h = 24;
            g2.setColor(status == Status.RESOLVED ? RESOLVED_BG : PENDING_BG);
            g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, h, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
