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

public class SubmitPanel extends JPanel {

    private final FeedbackManager manager;
    private final JPanel stage = new JPanel(new BorderLayout());
    private final JTextField srField = new JTextField();
    private final JTextArea messageArea = new JTextArea(5, 30);
    private final JScrollPane messageScroll = new JScrollPane(messageArea);
    private final JLabel counter = label("0 / " + Validator.MAX_MESSAGE, SMALL, MUTED);
    private final JLabel error = label(" ", SMALL_BOLD, DANGER);
    private final JLabel recordIdLabel = label("UV-0000", new Font("Georgia", Font.BOLD, 40), CARAMEL);
    private final ButtonGroup deptGroup = new ButtonGroup();
    private final ButtonGroup typeGroup = new ButtonGroup();
    private final List<ChipButton> deptChips = new ArrayList<>();
    private final List<ChipButton> typeChips = new ArrayList<>();
    private final RoundedPanel formCard;
    private final RoundedPanel doneCard;

    public SubmitPanel(FeedbackManager manager, Runnable onHome) {
        this.manager = manager;
        formCard = buildForm();
        doneCard = buildDone(onHome);

        stage.setOpaque(false);
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(CREAM);
        wrap.setBorder(new EmptyBorder(24, 24, 24, 24));
        wrap.add(stage);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CREAM);
        styleScroll(scroll);

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        reset();
    }

    private RoundedPanel buildForm() {
        RoundedPanel card = new RoundedPanel(26, FOAM, LINE);
        card.setFixedWidth(640);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        card.add(left(label("Send feedback", HEADING, ESPRESSO)));
        card.add(Box.createVerticalStrut(4));
        card.add(left(label("Tell us what you'd like to see improved on campus. Fields marked * are required.", SMALL, MUTED)));

        card.add(left(fieldLabel("SR-Code *")));
        styleInput(srField);
        fixHeight(srField);
        card.add(left(srField));
        card.add(Box.createVerticalStrut(4));
        card.add(left(label("Format: two digits, a dash, then five digits (e.g. 25-06680).", SMALL, MUTED)));

        card.add(left(fieldLabel("Department *")));
        JPanel deptRow = chipRow();
        for (String dept : CampusTracker.getDepartments()) {
            ChipButton chip = new ChipButton(dept);
            deptGroup.add(chip);
            deptChips.add(chip);
            deptRow.add(chip);
            deptRow.add(Box.createHorizontalStrut(8));
        }
        fixHeight(deptRow);
        card.add(left(deptRow));

        card.add(left(fieldLabel("What are you sending? *")));
        JPanel typeRow = chipRow();
        for (String type : new String[]{Suggestion.TYPE, Complaint.TYPE}) {
            ChipButton chip = new ChipButton(type);
            typeGroup.add(chip);
            typeChips.add(chip);
            typeRow.add(chip);
            typeRow.add(Box.createHorizontalStrut(8));
        }
        fixHeight(typeRow);
        card.add(left(typeRow));
        card.add(Box.createVerticalStrut(6));
        card.add(left(label("Suggestion: an idea to improve something.   Complaint: a problem that needs fixing.", SMALL, MUTED)));

        card.add(left(fieldLabel("Your message *")));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(BODY);
        messageArea.setForeground(TEXT);
        messageArea.setCaretColor(MOCHA);
        messageArea.setBackground(INPUT_BG);
        messageArea.setBorder(new EmptyBorder(9, 12, 9, 12));
        messageScroll.setBorder(new LineBorder(LINE, 1, true));
        messageScroll.getViewport().setBackground(INPUT_BG);
        styleScroll(messageScroll);
        messageArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { messageScroll.setBorder(new LineBorder(CARAMEL, 1, true)); }
            @Override public void focusLost(FocusEvent e) { messageScroll.setBorder(new LineBorder(LINE, 1, true)); }
        });
        messageArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCounter(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCounter(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCounter(); }
        });
        fixHeight(messageScroll);
        card.add(left(messageScroll));
        card.add(Box.createVerticalStrut(6));

        JPanel meta = new JPanel(new BorderLayout());
        meta.setOpaque(false);
        meta.add(error, BorderLayout.WEST);
        meta.add(counter, BorderLayout.EAST);
        fixHeight(meta);
        card.add(left(meta));
        card.add(Box.createVerticalStrut(14));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        RoundedButton submit = new RoundedButton("Submit feedback", RoundedButton.Style.PRIMARY);
        submit.addActionListener(e -> submit());
        RoundedButton clear = new RoundedButton("Clear form", RoundedButton.Style.SECONDARY);
        clear.addActionListener(e -> reset());
        buttons.add(submit);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(clear);
        fixHeight(buttons);
        card.add(left(buttons));
        return card;
    }

    private RoundedPanel buildDone(Runnable onHome) {
        RoundedPanel card = new RoundedPanel(26, FOAM, LINE);
        card.setFixedWidth(640);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 36, 40));

        card.add(left(label("Thank you!", HEADING, ESPRESSO)));
        card.add(Box.createVerticalStrut(6));
        card.add(left(label("Your feedback was saved with a status of Pending.", BODY, MUTED)));
        card.add(Box.createVerticalStrut(26));
        card.add(left(label("YOUR RECORD ID", SMALL_BOLD, CARAMEL)));
        card.add(left(recordIdLabel));
        card.add(Box.createVerticalStrut(4));
        card.add(left(label("Keep this ID so you can refer to your submission later.", SMALL, MUTED)));
        card.add(Box.createVerticalStrut(28));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        RoundedButton another = new RoundedButton("Send another", RoundedButton.Style.PRIMARY);
        another.addActionListener(e -> reset());
        RoundedButton home = new RoundedButton("Back to home", RoundedButton.Style.SECONDARY);
        home.addActionListener(e -> onHome.run());
        buttons.add(another);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(home);
        fixHeight(buttons);
        card.add(left(buttons));
        return card;
    }

    private void submit() {
        String srCode = srField.getText().trim();
        String department = selected(deptChips);
        String type = selected(typeChips);
        String message = messageArea.getText().trim();

        if (!Validator.isValidSrCode(srCode)) {
            showError("Enter a valid SR-Code, for example 25-06680.");
            srField.requestFocusInWindow();
        } else if (department == null) {
            showError("Please choose your department.");
        } else if (type == null) {
            showError("Please choose Suggestion or Complaint.");
        } else if (!Validator.isValidMessage(message)) {
            showError("Write a message of up to " + Validator.MAX_MESSAGE + " characters.");
            messageArea.requestFocusInWindow();
        } else {
            Feedback saved = manager.addFeedback(type, srCode, department, message);
            recordIdLabel.setText(saved.getRecordId());
            showStage(doneCard);
        }
    }

    public void reset() {
        srField.setText("");
        messageArea.setText("");
        deptGroup.clearSelection();
        typeGroup.clearSelection();
        error.setText(" ");
        showStage(formCard);
    }

    private void showStage(JComponent card) {
        stage.removeAll();
        stage.add(card, BorderLayout.CENTER);
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

    private static JLabel fieldLabel(String text) {
        JLabel l = label(text, SMALL_BOLD, TEXT);
        l.setBorder(new EmptyBorder(18, 0, 6, 0));
        return l;
    }

    private static JPanel chipRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        return row;
    }
}
