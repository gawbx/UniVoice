package univoice.ui;

import univoice.service.AdminAuth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static univoice.ui.Theme.*;

public class AdminLoginPanel extends JPanel {

    @SuppressWarnings("unused")
    private final AdminAuth auth;
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel message = label(" ", SMALL_BOLD, DANGER);

    public AdminLoginPanel(AdminAuth auth, Runnable onSuccess, Runnable onCancel) {
        this.auth = auth;
        setLayout(new GridBagLayout());
        setBackground(CREAM);

        RoundedPanel card = new RoundedPanel(26, FOAM, LINE);
        card.setFixedWidth(440);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 34, 30, 34));

        card.add(left(label("Administrator sign in", HEADING, ESPRESSO)));
        card.add(Box.createVerticalStrut(6));
        card.add(left(wrapped("Enter the admin password to manage campus feedback.", SMALL, MUTED, 360)));
        card.add(Box.createVerticalStrut(22));
        card.add(left(label("Password", SMALL_BOLD, TEXT)));
        card.add(Box.createVerticalStrut(6));
        styleInput(passwordField);
        fixHeight(passwordField);
        card.add(left(passwordField));
        card.add(Box.createVerticalStrut(8));
        card.add(left(message));
        card.add(Box.createVerticalStrut(14));

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        RoundedButton signIn = new RoundedButton("Sign in", RoundedButton.Style.PRIMARY);
        RoundedButton cancel = new RoundedButton("Cancel", RoundedButton.Style.SECONDARY);
        buttons.add(signIn);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(cancel);
        fixHeight(buttons);
        card.add(left(buttons));

        Runnable attempt = () -> {
            String typed = new String(passwordField.getPassword());
            if (auth.login(typed)) {
                reset();
                onSuccess.run();
            } else if (auth.isLocked()) {
                JOptionPane.showMessageDialog(this, "Too many failed attempts. Returning to the home screen.",
                        "Access locked", JOptionPane.WARNING_MESSAGE);
                auth.reset();
                reset();
                onCancel.run();
            } else {
                message.setText("Incorrect password. Attempts left: " + auth.getAttemptsLeft());
                passwordField.setText("");
            }
        };
        signIn.addActionListener(e -> attempt.run());
        passwordField.addActionListener(e -> attempt.run());
        cancel.addActionListener(e -> {
            reset();
            onCancel.run();
        });

        add(card);
    }

    public void reset() {
        passwordField.setText("");
        message.setText(" ");
        SwingUtilities.invokeLater(passwordField::requestFocusInWindow);
    }
}
