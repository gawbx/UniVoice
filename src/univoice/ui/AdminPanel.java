package univoice.ui;

import univoice.model.Complaint;
import univoice.model.Feedback;
import univoice.model.Status;
import univoice.service.FeedbackManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

import static univoice.ui.Theme.*;

/** Administrator dashboard (dark mode): view, search, resolve, delete, and moderate records. */
public class AdminPanel extends JPanel implements Responsive {

    private static final String[] COLUMNS = {"Record ID", "SR-Code", "Dept", "Type", "Status", "Filed", "Message"};
    private static final int[] WIDTHS = {100, 104, 78, 112, 116, 126, 220};
    private static final int[] COMPACT_WIDTHS = {92, 100, 64, 104};
    /** Columns shown in bold: Record ID, SR-Code, Dept, Type. */
    private static final boolean[] BOLD = {true, true, true, true, false, false, false};
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM d, HH:mm", Locale.ENGLISH);
    private static final int[] COMPACT_COLUMNS = {0, 1, 2, 4};
    private static final String[] TROLL_REASONS = {
            "Troll / prank submission", "Spam or duplicate", "Offensive or abusive language",
            "Not campus-related", "Other"};

    private final FeedbackManager manager;
    private final String adminId;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableColumn[] allColumns = new TableColumn[COLUMNS.length];
    private final SearchField searchField = new SearchField("Search SR-Code or record ID");
    private final JLabel totalValue = label("0", STAT, TEXT);
    private final JLabel pendingValue = label("0", STAT, PENDING);
    private final JLabel resolvedValue = label("0", STAT, RESOLVED);
    private final JLabel complaintValue = label("0", STAT, PRIMARY);
    private final JLabel flash = label(" ", SMALL_BOLD, RESOLVED);
    private final JLabel countLabel = label(" ", SMALL, MUTED);

    private final RoundedButton resolveBtn = new RoundedButton("Mark as Resolved", RoundedButton.Style.PRIMARY);
    private final RoundedButton pendingBtn = new RoundedButton("Reopen as Pending", RoundedButton.Style.SECONDARY);
    private final RoundedButton deleteBtn = new RoundedButton("Delete resolved record", RoundedButton.Style.DANGER);
    private final RoundedButton trollBtn = new RoundedButton("Remove as troll / spam", RoundedButton.Style.DANGER);

    private final JPanel detailBody = vstack();
    private final JPanel detailEmpty = vstack();
    private final JPanel detailActions = vstack();
    private final RoundedPanel detailsCard = RoundedPanel.card();
    private final RoundedPanel tableCard = RoundedPanel.card();
    private final JPanel tableStage = transparent(new CardLayout());
    private final JPanel top = vstack();
    private final JPanel toolbar = transparent(new BorderLayout(12, 10));
    private final JPanel chips = WrapLayout.row(8, 6);
    private final JPanel searchWrap = transparent(new BorderLayout(8, 0));
    private final JLabel heading = label("Admin dashboard", HEADING, TEXT);
    private final JScrollPane detailsScroll;
    private Timer flashTimer;

    private List<Feedback> shown = new ArrayList<>();
    private Status filter = null;
    private boolean compact = false;

    public AdminPanel(FeedbackManager manager, String adminId) {
        this.manager = manager;
        this.adminId = adminId;
        setBackground(BG);

        buildTop();
        buildTable();
        detailsScroll = buildDetails();

        resolveBtn.addActionListener(e -> changeStatus(Status.RESOLVED));
        pendingBtn.addActionListener(e -> changeStatus(Status.PENDING));
        deleteBtn.addActionListener(e -> deleteSelected());
        trollBtn.addActionListener(e -> removeTroll());

        arrange();
        refresh();
    }

    // ------------------------------------------------------------- building

    private void buildTop() {
        JPanel titleRow = transparent(new BorderLayout(12, 4));
        JPanel titles = vstack();
        titles.add(left(caption("Administrator  ·  " + adminId)));
        titles.add(Box.createVerticalStrut(4));
        titles.add(left(heading));
        titles.add(Box.createVerticalStrut(2));
        titles.add(left(label("Review, resolve, and clean up campus feedback.", SMALL, MUTED)));
        titleRow.add(titles, BorderLayout.CENTER);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(titleRow);
        top.add(Box.createVerticalStrut(16));

        JPanel stats = transparent(new GridFlowLayout(150, 4, 12, 12));
        stats.add(statCard(totalValue, "Total records"));
        stats.add(statCard(pendingValue, "Pending"));
        stats.add(statCard(resolvedValue, "Resolved"));
        stats.add(statCard(complaintValue, "Complaints"));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(stats);
        top.add(Box.createVerticalStrut(16));

        ButtonGroup group = new ButtonGroup();
        ChipButton all = new ChipButton("All");
        ChipButton pending = new ChipButton("Pending");
        ChipButton resolved = new ChipButton("Resolved");
        for (ChipButton c : new ChipButton[]{all, pending, resolved}) {
            group.add(c);
            chips.add(c);
        }
        all.setSelected(true);
        all.addActionListener(e -> { filter = null; refresh(); });
        pending.addActionListener(e -> { filter = Status.PENDING; refresh(); });
        resolved.addActionListener(e -> { filter = Status.RESOLVED; refresh(); });

        styleInput(searchField);
        searchField.setPreferredSize(new Dimension(260, 40));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        RoundedButton clear = new RoundedButton("Clear", RoundedButton.Style.SECONDARY);
        clear.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        clear.addActionListener(e -> searchField.setText(""));
        searchWrap.add(searchField, BorderLayout.CENTER);
        searchWrap.add(clear, BorderLayout.EAST);

        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(toolbar);
        top.add(Box.createVerticalStrut(10));

        JPanel infoRow = transparent(new BorderLayout(10, 0));
        infoRow.add(countLabel, BorderLayout.WEST);
        infoRow.add(flash, BorderLayout.EAST);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixHeight(infoRow);
        top.add(infoRow);
        top.add(Box.createVerticalStrut(8));
    }

    private void arrangeToolbar() {
        toolbar.removeAll();
        if (compact) {
            toolbar.add(searchWrap, BorderLayout.NORTH);
            toolbar.add(chips, BorderLayout.CENTER);
        } else {
            toolbar.add(chips, BorderLayout.CENTER);
            toolbar.add(searchWrap, BorderLayout.EAST);
        }
    }

    private RoundedPanel statCard(JLabel value, String caption) {
        RoundedPanel p = RoundedPanel.card();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(14, 18, 14, 18));
        p.add(left(caption(caption)));
        p.add(Box.createVerticalStrut(4));
        p.add(left(value));
        return p;
    }

    private void buildTable() {
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setBorder(null);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.setAutoCreateColumnsFromModel(false);
        for (int i = 0; i < COLUMNS.length; i++) {
            allColumns[i] = table.getColumnModel().getColumn(i);
            allColumns[i].setPreferredWidth(WIDTHS[i]);
        }
        allColumns[4].setCellRenderer(new StatusRenderer());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBackground(TABLE_HEAD_BG);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetails();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBackground(SURFACE);
        styleScroll(scroll);

        JPanel empty = transparent(new GridBagLayout());
        JPanel emptyText = vstack();
        JLabel e1 = label("No records to show", SUBHEAD, TEXT);
        JLabel e2 = label("Try another filter or clear the search.", SMALL, MUTED);
        e1.setAlignmentX(Component.CENTER_ALIGNMENT);
        e2.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyText.add(e1);
        emptyText.add(Box.createVerticalStrut(4));
        emptyText.add(e2);
        empty.add(emptyText);

        tableStage.add(scroll, "table");
        tableStage.add(empty, "empty");

        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(6, 6, 6, 6));
        tableCard.add(tableStage, BorderLayout.CENTER);
    }

    private JScrollPane buildDetails() {
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));
        detailsCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        detailsCard.add(left(label("Record details", SUBHEAD, TEXT)));
        detailsCard.add(Box.createVerticalStrut(12));

        detailEmpty.add(wrapped("Select a record from the table to see its full details and actions.", SMALL, MUTED));
        detailEmpty.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsCard.add(detailEmpty);

        detailBody.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsCard.add(detailBody);
        detailsCard.add(Box.createVerticalStrut(16));

        deleteBtn.setToolTipText("Only Resolved records can be deleted");
        trollBtn.setToolTipText("Removes prank, spam, or abusive submissions, even if still Pending");
        detailActions.add(resolveBtn.fullWidth());
        detailActions.add(Box.createVerticalStrut(8));
        detailActions.add(pendingBtn.fullWidth());
        detailActions.add(Box.createVerticalStrut(8));
        detailActions.add(deleteBtn.fullWidth());
        detailActions.add(Box.createVerticalStrut(16));
        JPanel divider = new JPanel();
        divider.setOpaque(false);
        divider.setBorder(new MatteBorder(1, 0, 0, 0, LINE));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailActions.add(divider);
        detailActions.add(Box.createVerticalStrut(14));
        detailActions.add(left(caption("Moderation")));
        detailActions.add(Box.createVerticalStrut(6));
        detailActions.add(wrapped("For troll, prank, spam, or abusive complaints. Works on Pending records "
                + "and is logged with your admin ID.", SMALL, MUTED));
        detailActions.add(Box.createVerticalStrut(10));
        detailActions.add(trollBtn.fullWidth());
        detailActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsCard.add(detailActions);

        JPanel holder = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(compact ? d.width : 300, d.height);
            }
        };
        holder.setOpaque(false);
        holder.add(detailsCard, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(compact ? d.width : 312, d.height);
            }
        };
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScroll(sp);
        return sp;
    }

    /** Places the parts for desktop or mobile view. */
    private void arrange() {
        removeAll();
        arrangeToolbar();
        applyColumns();
        if (compact) {
            setLayout(new BorderLayout());
            JPanel col = vstack();
            top.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(top);
            tableCard.setPreferredSize(new Dimension(200, 330));
            tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
            tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(tableCard);
            col.add(Box.createVerticalStrut(14));
            detailsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(detailsCard);
            Page page = new Page(col, 900, false);
            page.setPadding(14, 16);
            add(page.inScroll(), BorderLayout.CENTER);
        } else {
            setLayout(new BorderLayout(18, 0));
            setBorder(new EmptyBorder(24, 28, 22, 28));
            tableCard.setPreferredSize(null);
            tableCard.setMaximumSize(null);
            JPanel holder = (JPanel) detailsScroll.getViewport().getView();
            holder.removeAll();
            holder.add(detailsCard, BorderLayout.NORTH);
            JPanel center = transparent(new BorderLayout(0, 0));
            center.add(top, BorderLayout.NORTH);
            center.add(tableCard, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);
            add(detailsScroll, BorderLayout.EAST);
        }
        if (compact) {
            setBorder(null);
        }
        revalidate();
        repaint();
    }

    private void applyColumns() {
        while (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
        }
        if (compact) {
            for (int k = 0; k < COMPACT_COLUMNS.length; k++) {
                TableColumn c = allColumns[COMPACT_COLUMNS[k]];
                c.setMinWidth(40);
                c.setPreferredWidth(COMPACT_WIDTHS[k]);
                table.addColumn(c);
            }
        } else {
            for (int i = 0; i < allColumns.length; i++) {
                TableColumn c = allColumns[i];
                c.setMinWidth(i < allColumns.length - 1 ? WIDTHS[i] - 12 : 15);
                c.setPreferredWidth(WIDTHS[i]);
                table.addColumn(c);
            }
        }
    }

    // ------------------------------------------------------------- behavior

    public void refresh() {
        Feedback previous = selectedRecord();
        int keepId = previous == null ? -1 : previous.getId();

        shown = manager.listRecords(filter, searchField.getText());
        model.setRowCount(0);
        int reselect = -1;
        for (int i = 0; i < shown.size(); i++) {
            Feedback f = shown.get(i);
            model.addRow(new Object[]{f.getRecordId(), f.getSrCode(), f.getDepartment(), f.getType(),
                    f.getStatus().getLabel(), f.getDateFiled().format(SHORT_DATE), f.getMessage().replace('\n', ' ')});
            if (f.getId() == keepId) {
                reselect = i;
            }
        }
        totalValue.setText(String.valueOf(manager.count(null)));
        pendingValue.setText(String.valueOf(manager.count(Status.PENDING)));
        resolvedValue.setText(String.valueOf(manager.count(Status.RESOLVED)));
        int complaints = 0;
        for (Feedback f : manager.listRecords(null)) {
            if (f instanceof Complaint) {
                complaints++;
            }
        }
        complaintValue.setText(String.valueOf(complaints));
        countLabel.setText("Showing " + shown.size() + " of " + manager.count(null) + " records");
        ((CardLayout) tableStage.getLayout()).show(tableStage, shown.isEmpty() ? "empty" : "table");

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
        detailEmpty.setVisible(f == null);
        detailBody.setVisible(f != null);
        detailActions.setVisible(f != null);
        detailBody.removeAll();
        if (f != null) {
            JPanel idRow = transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
            idRow.add(new Badge(f.getType().toUpperCase(), f instanceof Complaint ? PRIMARY : ACCENT,
                    f instanceof Complaint ? PRIMARY_SOFT : SURFACE_ALT));
            idRow.add(Box.createHorizontalStrut(8));
            idRow.add(new Badge(f.getStatus().getLabel().toUpperCase(),
                    f.isResolved() ? RESOLVED : PENDING, f.isResolved() ? RESOLVED_BG : PENDING_BG));
            idRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            fixHeight(idRow);
            detailBody.add(idRow);
            detailBody.add(Box.createVerticalStrut(10));
            detailBody.add(left(label(f.getRecordId(), TITLE_SM, TEXT)));
            detailBody.add(Box.createVerticalStrut(12));

            JPanel facts = transparent(new GridFlowLayout(110, 2, 12, 12));
            facts.add(fact("SR-Code", f.getSrCode()));
            facts.add(fact("Dept", f.getDepartment()));
            facts.add(fact("Status", f.getStatus().getLabel()));
            facts.add(fact("Filed", f.getDateFiledText()));
            facts.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailBody.add(facts);
            detailBody.add(Box.createVerticalStrut(14));

            detailBody.add(left(caption("Message")));
            detailBody.add(Box.createVerticalStrut(6));
            RoundedPanel msgBox = new RoundedPanel(10, SURFACE_ALT, LINE);
            msgBox.setLayout(new BorderLayout());
            msgBox.setBorder(new EmptyBorder(10, 12, 10, 12));
            msgBox.add(wrapped(f.getMessage(), BODY, TEXT), BorderLayout.CENTER);
            msgBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailBody.add(msgBox);
            if (f instanceof Complaint) {
                detailBody.add(Box.createVerticalStrut(8));
                detailBody.add(wrapped(((Complaint) f).getUrgencyNote(), SMALL, MUTED));
            }

            resolveBtn.setVisible(!f.isResolved());
            pendingBtn.setVisible(f.isResolved());
            deleteBtn.setEnabled(f.isResolved());
        }
        detailsCard.revalidate();
        detailsCard.repaint();
        revalidate();
    }

    private static JPanel fact(String name, String value) {
        JPanel p = vstack();
        p.add(left(caption(name)));
        p.add(Box.createVerticalStrut(3));
        p.add(left(label(value, BODY_BOLD, TEXT)));
        return p;
    }

    private void changeStatus(Status newStatus) {
        Feedback f = selectedRecord();
        if (f == null) {
            return;
        }
        boolean ok;
        if (newStatus == Status.RESOLVED) {
            ok = Dialogs.confirm(this, Dialogs.Kind.INFO, "Mark " + f.getRecordId() + " as Resolved?",
                    "This marks the " + f.getType().toLowerCase() + " from " + f.getSrCode()
                            + " as handled. You can reopen it later if needed.", "Mark as Resolved", "Cancel");
        } else {
            ok = Dialogs.confirm(this, Dialogs.Kind.WARNING, "Reopen " + f.getRecordId() + "?",
                    "This moves the record back to Pending so it shows up as unresolved again.",
                    "Reopen as Pending", "Cancel");
        }
        if (ok) {
            manager.updateStatus(f.getId(), newStatus);
            showFlash(f.getRecordId() + " marked as " + newStatus.getLabel() + ".");
            refresh();
        }
    }

    private void deleteSelected() {
        Feedback f = selectedRecord();
        if (f == null) {
            return;
        }
        if (!f.isResolved()) {
            Dialogs.info(this, Dialogs.Kind.WARNING, "Cannot delete yet",
                    "Only Resolved records can be deleted. To remove a troll or spam submission, "
                            + "use \"Remove as troll / spam\" instead.");
            return;
        }
        boolean ok = Dialogs.confirm(this, Dialogs.Kind.DANGER, "Delete " + f.getRecordId() + "?",
                "This permanently removes the resolved record from the data file. This cannot be undone.",
                "Delete record", "Cancel");
        if (!ok) {
            return;
        }
        try {
            manager.deleteRecord(f.getId());
            showFlash(f.getRecordId() + " deleted.");
        } catch (IllegalStateException ex) {
            Dialogs.info(this, Dialogs.Kind.WARNING, "Cannot delete", ex.getMessage());
        }
        refresh();
    }

    private void removeTroll() {
        Feedback f = selectedRecord();
        if (f == null) {
            return;
        }
        String reason = Dialogs.confirmRemoval(this, "Remove " + f.getRecordId() + " as troll / spam?",
                "The " + f.getType().toLowerCase() + " from " + f.getSrCode() + " will be removed even though it is "
                        + f.getStatus().getLabel() + ". This cannot be undone. The removal is logged with your admin ID.",
                f.getRecordId(), TROLL_REASONS, "Remove record");
        if (reason == null) {
            return;
        }
        manager.removeInvalidRecord(f.getId(), reason, adminId);
        showFlash(f.getRecordId() + " removed (" + reason + ").");
        refresh();
    }

    private void showFlash(String text) {
        flash.setText(text);
        if (flashTimer != null) {
            flashTimer.stop();
        }
        flashTimer = new Timer(5000, e -> flash.setText(" "));
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    @Override
    public void setCompact(boolean compact) {
        if (this.compact == compact) {
            return;
        }
        this.compact = compact;
        heading.setFont(compact ? SUBHEAD : HEADING);
        arrange();
    }

    // ------------------------------------------------------------ rendering

    private static Color rowColor(int row, boolean selected) {
        return selected ? PRIMARY_SOFT : (row % 2 == 0 ? SURFACE : SURFACE_ALT);
    }

    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            int modelCol = t.convertColumnIndexToModel(col);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(BOLD[modelCol] ? BODY_BOLD : BODY);
            setForeground(modelCol == 5 || modelCol == 6 ? MUTED : TEXT);
            if (modelCol == 0) {
                setForeground(selected ? PRIMARY : TEXT);
            }
            setBackground(rowColor(row, selected));
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            setText(String.valueOf(value).toUpperCase());
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(CAPTION);
            setForeground(TABLE_HEAD_TEXT);
            setBackground(TABLE_HEAD_BG);
            return this;
        }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        private Status status = Status.PENDING;
        private Color rowBg = SURFACE;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            status = Status.fromString(String.valueOf(value));
            rowBg = rowColor(row, selected);
            setOpaque(false);
            setHorizontalAlignment(LEFT);
            setFont(SMALL_BOLD);
            setBorder(new EmptyBorder(0, 24, 0, 12));
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
            int w = fm.stringWidth(getText()) + 24;
            int h = 24;
            paintBadge(g2, 12, (getHeight() - h) / 2, w, h, status == Status.RESOLVED ? RESOLVED_BG : PENDING_BG);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Small rounded label used for the record type and status in the details card. */
    private static final class Badge extends JLabel {
        private final Color bg;

        Badge(String text, Color fg, Color bg) {
            super(text);
            this.bg = bg;
            setFont(CAPTION);
            setForeground(fg);
            setBorder(new EmptyBorder(4, 10, 4, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Text field that shows grey placeholder text while empty. */
    private static final class SearchField extends JTextField {
        private final String placeholder;

        SearchField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(MUTED);
                g2.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }
}
