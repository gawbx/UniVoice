package univoice.ui;

import univoice.CampusTracker;
import univoice.model.Complaint;
import univoice.model.Feedback;
import univoice.model.Suggestion;
import univoice.service.FeedbackManager;
import univoice.service.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import static univoice.ui.Theme.*;

/**
 * Student home: the feedback form. The SR-Code comes from the signed-in
 * account, so students only pick a department, a type, and write a message.
 */
public class SubmitPanel extends JPanel implements Responsive {

    private final FeedbackManager manager;
    private final String srCode;
    private final JPanel stage = vstack();
    private final JTextArea messageArea = new JTextArea(5, 20);
    private final JScrollPane messageScroll = new JScrollPane(messageArea);
    private final JLabel counter = label("0 / " + Validator.MAX_MESSAGE, SMALL, MUTED);
    private final WrapText error = new WrapText(" ", SMALL_BOLD, Theme.DANGER);
    private final JLabel recordIdLabel = label("UV-0000", RECORD_ID, PRIMARY);
    private final JPanel receipt = vstack();
    private final ButtonGroup deptGroup = new ButtonGroup();
    private final ButtonGroup typeGroup = new ButtonGroup();
    private final List<ChipButton> deptChips = new ArrayList<>();
    private final List<ChipButton> typeChips = new ArrayList<>();
    private final JPanel formCard;
    private final JPanel doneCard;
    private final Page page;
    private final JLabel greeting;

    public SubmitPanel(FeedbackManager manager, String srCode) {
        this.manager = manager;
        this.srCode = srCode;
        greeting = label("Hello, " + srCode, HEADING, TEXT);
        formCard = buildForm();
        doneCard = buildDone();

        JPanel col = vstack();
        col.add(left(caption("Student")));
        col.add(Box.createVerticalStrut(4));
        col.add(left(greeting));
        col.add(Box.createVerticalStrut(4));
        col.add(wrapped("Tell us what you'd like improved on campus or report a problem that needs fixing.",
                BODY, MUTED));
        col.add(Box.createVerticalStrut(20));
        stage.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(stage);

        page = new Page(col, 720, false);
        setLayout(new BorderLayout());
        add(page.inScroll(), BorderLayout.CENTER);
        resetForm();
    }

    private JPanel buildForm() {
        RoundedPanel card = RoundedPanel.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 28, 26, 28));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(left(label("Send feedback", SUBHEAD, TEXT)));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapped("All fields are required.", SMALL, MUTED));

        card.add(fieldLabel("SR-Code"));
        card.add(srCodeBox());

        card.add(fieldLabel("Department"));
        JPanel deptRow = WrapLayout.row(8, 8);
        for (String dept : CampusTracker.getDepartments()) {
            ChipButton chip = new ChipButton(dept);
            deptGroup.add(chip);
            deptChips.add(chip);
            deptRow.add(chip);
        }
        card.add(deptRow);

        card.add(fieldLabel("What are you sending?"));
        JPanel typeRow = WrapLayout.row(8, 8);
        for (String type : new String[]{Suggestion.TYPE, Complaint.TYPE}) {
            ChipButton chip = new ChipButton(type);
            typeGroup.add(chip);
            typeChips.add(chip);
            typeRow.add(chip);
        }
        card.add(typeRow);
        card.add(Box.createVerticalStrut(6));
        card.add(wrapped("Suggestion: an idea to improve something.  Complaint: a problem that needs fixing.",
                SMALL, MUTED));

        card.add(fieldLabel("Your message"));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(BODY);
        messageArea.setForeground(TEXT);
        messageArea.setCaretColor(PRIMARY);
        messageArea.setBackground(INPUT_BG);
        messageArea.setSelectionColor(PRIMARY_SOFT);
        messageArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        messageScroll.setBorder(new LineBorder(LINE, 1, true));
        messageScroll.getViewport().setBackground(INPUT_BG);
        messageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScroll(messageScroll);
        messageArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { messageScroll.setBorder(new LineBorder(PRIMARY, 1, true)); }
            @Override public void focusLost(FocusEvent e) { messageScroll.setBorder(new LineBorder(LINE, 1, true)); }
        });
        messageArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCounter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCounter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCounter(); }
        });
        messageScroll.setPreferredSize(new Dimension(200, 150));
        messageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.add(left(messageScroll));
        card.add(Box.createVerticalStrut(6));

        JPanel meta = transparent(new BorderLayout(10, 0));
        meta.add(error, BorderLayout.CENTER);
        meta.add(counter, BorderLayout.EAST);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(meta);
        card.add(Box.createVerticalStrut(16));

        JPanel buttons = WrapLayout.row(10, 8);
        RoundedButton submit = new RoundedButton("Submit feedback", RoundedButton.Style.PRIMARY);
        submit.addActionListener(e -> submit());
        RoundedButton clear = new RoundedButton("Clear form", RoundedButton.Style.SECONDARY);
        clear.addActionListener(e -> confirmClear());
        buttons.add(submit);
        buttons.add(clear);
        card.add(buttons);
        return card;
    }

    /** Read-only SR-Code taken from the signed-in account. */
    private JPanel srCodeBox() {
        RoundedPanel box = new RoundedPanel(10, SURFACE_ALT, LINE);
        box.setLayout(new BorderLayout(10, 0));
        box.setBorder(new EmptyBorder(10, 14, 10, 14));
        box.add(label(srCode, BODY_BOLD, TEXT), BorderLayout.WEST);
        box.add(label("from your account", SMALL, MUTED), BorderLayout.EAST);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        fixHeight(box);
        return box;
    }

    private JPanel buildDone() {
        RoundedPanel card = RoundedPanel.card().withAccent(RESOLVED);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 30, 30, 30));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(left(label("Thank you!", HEADING, TEXT)));
        card.add(Box.createVerticalStrut(6));
        card.add(wrapped("Your feedback was saved with a status of Pending and sent to the administrators.",
                BODY, MUTED));
        card.add(Box.createVerticalStrut(22));
        card.add(left(caption("Your record ID")));
        card.add(left(recordIdLabel));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapped("Keep this ID so you can refer to your submission later.", SMALL, MUTED));
        card.add(Box.createVerticalStrut(18));
        receipt.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(receipt);
        card.add(Box.createVerticalStrut(24));

        JPanel buttons = WrapLayout.row(10, 8);
        RoundedButton another = new RoundedButton("Send another", RoundedButton.Style.PRIMARY);
        another.addActionListener(e -> resetForm());
        buttons.add(another);
        card.add(buttons);
        return card;
    }

    private void submit() {
        String department = selected(deptChips);
        String type = selected(typeChips);
        String message = messageArea.getText().trim();

        if (!Validator.isValidSrCode(srCode)) {
            showError("Your account's SR-Code is not valid. Please sign in again.");
        } else if (department == null) {
            showError("Please choose your department.");
        } else if (type == null) {
            showError("Please choose Suggestion or Complaint.");
        } else if (!Validator.isValidMessage(message)) {
            showError("Write a message of 1 to " + Validator.MAX_MESSAGE + " characters.");
            messageArea.requestFocusInWindow();
        } else {
            boolean ok = Dialogs.confirm(this, Dialogs.Kind.INFO, "Submit this " + type.toLowerCase() + "?",
                    "Your " + type.toLowerCase() + " for " + department + " will be saved as Pending and sent to "
                            + "the administrators. You can't edit it after sending.",
                    "Submit", "Keep editing");
            if (!ok) {
                return;
            }
            Feedback saved = manager.addFeedback(type, srCode, department, message);
            recordIdLabel.setText(saved.getRecordId());
            fillReceipt(saved);
            showStage(doneCard);
        }
    }

    private void fillReceipt(Feedback f) {
        receipt.removeAll();
        JPanel grid = transparent(new GridFlowLayout(120, 4, 12, 12));
        grid.add(fact("SR-Code", f.getSrCode()));
        grid.add(fact("Dept", f.getDepartment()));
        grid.add(fact("Type", f.getType()));
        grid.add(fact("Status", f.getStatus().getLabel()));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        receipt.add(grid);
    }

    private static JPanel fact(String name, String value) {
        JPanel p = vstack();
        p.add(left(caption(name)));
        p.add(Box.createVerticalStrut(3));
        p.add(left(label(value, BODY_BOLD, TEXT)));
        return p;
    }

    private void confirmClear() {
        if (!hasDraft()) {
            resetForm();
            return;
        }
        if (Dialogs.confirm(this, Dialogs.Kind.WARNING, "Clear the form?",
                "Your selections and the message you typed will be removed.", "Clear form", "Keep it")) {
            resetForm();
        }
    }

    /** True when the student has started filling in the form. */
    public boolean hasDraft() {
        return formCard.isShowing() && (!messageArea.getText().trim().isEmpty()
                || selected(deptChips) != null || selected(typeChips) != null);
    }

    public void resetForm() {
        messageArea.setText("");
        deptGroup.clearSelection();
        typeGroup.clearSelection();
        error.setText(" ");
        showStage(formCard);
    }

    private void showStage(JComponent card) {
        stage.removeAll();
        stage.add(card);
        stage.revalidate();
        stage.repaint();
    }

    private void showError(String text) {
        error.setText(text);
    }

    private void updateCounter() {
        int length = messageArea.getText().length();
        counter.setText(length + " / " + Validator.MAX_MESSAGE);
        counter.setForeground(length > Validator.MAX_MESSAGE ? DANGER : MUTED);
    }

    private static String selected(List<ChipButton> chips) {
        for (ChipButton chip : chips) {
            if (chip.isSelected()) {
                return chip.getText();
            }
        }
        return null;
    }

    private static JComponent fieldLabel(String text) {
        JLabel l = caption(text);
        l.setBorder(new EmptyBorder(18, 0, 8, 0));
        return left(l);
    }

    @Override
    public void setCompact(boolean compact) {
        greeting.setFont(compact ? SUBHEAD : HEADING);
        page.setPadding(compact ? 14 : 32, compact ? 16 : 28);
        int pad = compact ? 18 : 28;
        formCard.setBorder(new EmptyBorder(pad - 2, pad, pad - 2, pad));
        doneCard.setBorder(new EmptyBorder(pad + 4, pad, pad + 2, pad));
        recordIdLabel.setFont(compact ? TITLE_SM : RECORD_ID);
        revalidate();
    }
}
