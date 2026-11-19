package univoice.ui;

import univoice.service.AuthService;
import univoice.service.Role;
import univoice.service.Session;
import univoice.service.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

import static univoice.ui.Theme.*;

/**
 * Landing page. One sign-in form serves everyone: the format of the login ID
 * decides the account type. An SR-Code opens the student side only and an
 * ADM-#### ID opens the administrator side only.
 */
public class LoginPanel extends JPanel implements Responsive {

    private final AuthService auth;
    private final Consumer<Session> onLogin;

    private final JTextField idField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JPanel confirmBlock = vstack();
    private final RolePill rolePill = new RolePill();
    private final WrapText message = new WrapText(" ", SMALL_BOLD, Theme.DANGER);
    private final RoundedButton signIn = new RoundedButton("Sign in", RoundedButton.Style.PRIMARY);
    private final JLabel title = label("<html>Your campus feedback,<br>all in one place.</html>", TITLE, TEXT);
    private final JPanel steps;
    private final Page page;
    private boolean registering = false;
    private Timer lockTimer;

    public LoginPanel(AuthService auth, Consumer<Session> onLogin) {
        this.auth = auth;
        this.onLogin = onLogin;

        JPanel grid = transparent(new GridFlowLayout(340, 2, 40, 28));
        steps = buildSteps();
        grid.add(buildHero());
        grid.add(buildCard());

        page = new Page(grid, 1040, true);
        setLayout(new BorderLayout());
        add(page.inScroll(), BorderLayout.CENTER);

        DocumentListener idWatcher = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onIdChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onIdChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onIdChanged(); }
        };
        idField.getDocument().addDocumentListener(idWatcher);
        signIn.addActionListener(e -> attempt());
        idField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> {
            if (registering) {
                confirmField.requestFocusInWindow();
            } else {
                attempt();
            }
        });
        confirmField.addActionListener(e -> attempt());
        reset();
    }

    private JPanel buildHero() {
        JPanel col = vstack();
        col.setBorder(new EmptyBorder(8, 0, 0, 0));
        col.add(left(label("BATANGAS STATE UNIVERSITY  ·  ALANGILAN", CAPTION, PRIMARY)));
        col.add(Box.createVerticalStrut(12));
        col.add(left(title));
        col.add(Box.createVerticalStrut(14));
        col.add(wrapped("Report a problem or share an idea. Your message goes straight to the "
                + "administrators who can act on it.", new Font(BODY.getFamily(), Font.PLAIN, 16), MUTED));
        col.add(Box.createVerticalStrut(26));
        col.add(left(steps));
        return col;
    }

    private JPanel buildSteps() {
        JPanel s = vstack();
        s.add(step("1", "Sign in with your SR-Code", "First time? Pick a password and your account is created."));
        s.add(Box.createVerticalStrut(14));
        s.add(step("2", "Send a suggestion or complaint", "Choose your department and describe what's happening."));
        s.add(Box.createVerticalStrut(14));
        s.add(step("3", "Keep your record ID", "Administrators review it and mark it Resolved once handled."));
        return s;
    }

    private JPanel step(String number, String head, String text) {
        JPanel p = transparent(new BorderLayout(14, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badge = new JLabel(number, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PRIMARY_SOFT);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(SMALL_BOLD);
        badge.setForeground(PRIMARY);
        badge.setPreferredSize(new Dimension(30, 30));
        JPanel badgeWrap = transparent(new BorderLayout());
        badgeWrap.add(badge, BorderLayout.NORTH);

        JPanel t = vstack();
        t.add(left(label(head, BODY_BOLD, TEXT)));
        t.add(Box.createVerticalStrut(3));
        t.add(wrapped(text, SMALL, MUTED));
        p.add(badgeWrap, BorderLayout.WEST);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private RoundedPanel buildCard() {
        RoundedPanel card = RoundedPanel.card().withAccent(PRIMARY);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 28, 30));

        card.add(left(label("Sign in", HEADING, TEXT)));
        card.add(Box.createVerticalStrut(4));
        card.add(wrapped("Use your SR-Code if you're a student, or your administrator ID.", SMALL, MUTED));
        card.add(Box.createVerticalStrut(20));

        card.add(left(caption("Login ID")));
        card.add(Box.createVerticalStrut(6));
        styleInput(idField);
        idField.setFont(BODY_BOLD);
        fixHeight(idField);
        card.add(left(idField));
        card.add(Box.createVerticalStrut(8));
        card.add(left(rolePill));
        card.add(Box.createVerticalStrut(14));

        card.add(left(caption("Password")));
        card.add(Box.createVerticalStrut(6));
        styleInput(passwordField);
        fixHeight(passwordField);
        card.add(left(passwordField));

        confirmBlock.add(Box.createVerticalStrut(14));
        confirmBlock.add(left(caption("Confirm new password")));
        confirmBlock.add(Box.createVerticalStrut(6));
        styleInput(confirmField);
        fixHeight(confirmField);
        confirmBlock.add(left(confirmField));
        confirmBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(confirmBlock);

        card.add(Box.createVerticalStrut(10));
        card.add(message);
        card.add(Box.createVerticalStrut(12));
        card.add(signIn.fullWidth());
        card.add(Box.createVerticalStrut(18));

        JPanel formats = vstack();
        formats.setAlignmentX(Component.LEFT_ALIGNMENT);
        formats.add(left(caption("Accepted formats")));
        formats.add(Box.createVerticalStrut(8));
        formats.add(formatRow("Student", "25-06680", "SR-Code"));
        formats.add(Box.createVerticalStrut(6));
        formats.add(formatRow("Administrator", "ADM-0001", "Admin ID"));
        card.add(formats);
        return card;
    }

    private JPanel formatRow(String who, String example, String what) {
        JPanel row = transparent(new BorderLayout(10, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(label(who, SMALL_BOLD, TEXT), BorderLayout.WEST);
        row.add(label("<html>" + what + " &nbsp;<b>" + example + "</b></html>", SMALL, MUTED), BorderLayout.EAST);
        fixHeight(row);
        return row;
    }

    // ------------------------------------------------------------- behavior

    private void onIdChanged() {
        if (registering) {
            setRegistering(false);
        }
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            rolePill.show(null, false);
        } else {
            rolePill.show(Validator.detectRole(id), true);
        }
        if (!auth.isLocked()) {
            message.setText(" ");
        }
    }

    private void setRegistering(boolean on) {
        registering = on;
        confirmBlock.setVisible(on);
        confirmField.setText("");
        signIn.setText(on ? "Create account & sign in" : "Sign in");
        revalidate();
        repaint();
    }

    private void attempt() {
        if (auth.isLocked()) {
            startLockCountdown();
            return;
        }
        String id = idField.getText().trim();
        String password = new String(passwordField.getPassword());
        Role role = Validator.detectRole(id);

        if (id.isEmpty()) {
            showError("Enter your SR-Code or administrator ID.");
            idField.requestFocusInWindow();
            return;
        }
        if (role == null) {
            showError("Unrecognized format. Use 25-06680 (student) or ADM-0001 (admin).");
            idField.requestFocusInWindow();
            return;
        }

        if (role == Role.STUDENT && auth.needsRegistration(id)) {
            if (!registering) {
                setRegistering(true);
                showInfo("New SR-Code. Confirm a password to create your account.");
                (password.isEmpty() ? passwordField : confirmField).requestFocusInWindow();
                return;
            }
            String confirm = new String(confirmField.getPassword());
            if (!Validator.isValidPassword(password)) {
                showError("Use at least " + Validator.MIN_PASSWORD + " characters for your password.");
                passwordField.requestFocusInWindow();
                return;
            }
            if (!password.equals(confirm)) {
                showError("The passwords don't match.");
                confirmField.setText("");
                confirmField.requestFocusInWindow();
                return;
            }
            boolean ok = Dialogs.confirm(this, Dialogs.Kind.WARNING, "Create a student account?",
                    "This creates a UniVoice account for SR-Code " + id + ". Only the owner of this SR-Code "
                            + "should use it. Remember your password; it can't be recovered in the app.",
                    "Create account", "Go back");
            if (!ok) {
                return;
            }
            try {
                Session s = auth.registerStudent(id, password);
                finish(s);
            } catch (AuthService.AuthException ex) {
                showError(ex.getMessage());
            }
            return;
        }

        try {
            Session s = auth.login(id, password);
            finish(s);
        } catch (AuthService.AuthException ex) {
            passwordField.setText("");
            if (auth.isLocked()) {
                Dialogs.info(this, Dialogs.Kind.DANGER, "Sign-in locked", ex.getMessage());
                startLockCountdown();
            } else {
                showError(ex.getMessage());
                passwordField.requestFocusInWindow();
            }
        }
    }

    private void finish(Session s) {
        reset();
        onLogin.accept(s);
    }

    private void startLockCountdown() {
        if (lockTimer != null) {
            lockTimer.stop();
        }
        signIn.setEnabled(false);
        Runnable tick = () -> {
            int left = auth.secondsLocked();
            if (left <= 0) {
                lockTimer.stop();
                signIn.setEnabled(true);
                message.setText(" ");
            } else {
                showError("Too many failed attempts. Try again in " + left + "s.");
            }
        };
        lockTimer = new Timer(500, e -> tick.run());
        tick.run();
        lockTimer.start();
    }

    private void showError(String text) {
        message.setForeground(DANGER);
        message.setText(text);
    }

    private void showInfo(String text) {
        message.setForeground(PRIMARY);
        message.setText(text);
    }

    public void reset() {
        idField.setText("");
        passwordField.setText("");
        setRegistering(false);
        rolePill.show(null, false);
        if (!auth.isLocked()) {
            message.setText(" ");
            signIn.setEnabled(true);
        }
        SwingUtilities.invokeLater(idField::requestFocusInWindow);
    }

    @Override
    public void setCompact(boolean compact) {
        title.setFont(compact ? TITLE_SM : TITLE);
        steps.setVisible(!compact);
        page.setPadding(compact ? 16 : 32, compact ? 18 : 28);
        revalidate();
    }

    /** Live label under the ID field showing which account type the format points to. */
    private static final class RolePill extends JLabel {
        private Color bg;

        RolePill() {
            setFont(CAPTION);
            setBorder(new EmptyBorder(4, 10, 4, 10));
        }

        void show(Role role, boolean typed) {
            if (!typed) {
                setText("Students: 25-06680   ·   Admins: ADM-0001");
                setForeground(MUTED);
                bg = null;
            } else if (role == Role.STUDENT) {
                setText("STUDENT ACCOUNT  ·  student access only");
                setForeground(PRIMARY);
                bg = PRIMARY_SOFT;
            } else if (role == Role.ADMIN) {
                setText("ADMINISTRATOR ACCOUNT  ·  admin access only");
                setForeground(WHITE);
                bg = new Color(0x2A2B2F);
            } else {
                setText("Format not recognized yet");
                setForeground(DANGER);
                bg = DANGER_BG;
            }
            setBorder(new EmptyBorder(4, bg == null ? 0 : 10, 4, 10));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (bg != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
