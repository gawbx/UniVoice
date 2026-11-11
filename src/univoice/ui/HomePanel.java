package univoice.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static univoice.ui.Theme.*;

public class HomePanel extends JPanel {

    public HomePanel(Runnable onSend, Runnable onAdmin) {
        setLayout(new GridBagLayout());
        setBackground(CREAM);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(left(label("CAMPUS FEEDBACK  -  BATSTATE-U ALANGILAN", SMALL_BOLD, CARAMEL)));
        col.add(Box.createVerticalStrut(10));
        col.add(left(label("<html>Your campus feedback,<br>all in one place.</html>", TITLE, ESPRESSO)));
        col.add(Box.createVerticalStrut(12));
        col.add(left(wrapped("Report a problem or share an idea. Your message goes straight to the administrators who can act on it.",
                new Font(Font.SANS_SERIF, Font.PLAIN, 16), MUTED, 540)));
        col.add(Box.createVerticalStrut(28));

        JPanel cards = new JPanel(new GridLayout(1, 2, 18, 0));
        cards.setOpaque(false);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        cards.add(card("I'm a student, faculty, or staff member",
                "Send a suggestion or file a complaint about anything on campus. It takes about a minute.",
                "Send feedback", RoundedButton.Style.PRIMARY, onSend));
        cards.add(card("I'm an administrator",
                "Sign in to review submissions, mark them as resolved, and clear out finished records.",
                "Sign in as admin", RoundedButton.Style.SECONDARY, onAdmin));
        col.add(cards);
        col.add(Box.createVerticalStrut(30));

        JPanel steps = new JPanel(new GridLayout(1, 3, 18, 0));
        steps.setOpaque(false);
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        steps.add(step("1", "Tell us what's happening", "Enter your SR-Code, pick your department, and write your message."));
        steps.add(step("2", "Keep your record ID", "Every submission is saved as Pending and gets a unique record ID."));
        steps.add(step("3", "Administrators take action", "Admins review the record and mark it Resolved once it's handled."));
        col.add(steps);

        add(col);
    }

    private RoundedPanel card(String title, String text, String buttonText, RoundedButton.Style style, Runnable action) {
        RoundedPanel p = new RoundedPanel(24, FOAM, LINE);
        p.setFixedWidth(400);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 26, 24, 26));
        p.add(left(wrapped(title, SUBHEAD, ESPRESSO, 340)));
        p.add(Box.createVerticalStrut(8));
        p.add(left(wrapped(text, BODY, MUTED, 340)));
        p.add(Box.createVerticalStrut(20));
        RoundedButton b = new RoundedButton(buttonText, style);
        b.addActionListener(e -> action.run());
        p.add(left(b));
        return p;
    }

    private JPanel step(String number, String title, String text) {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);

        JLabel badge = new JLabel(number, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARAMEL);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(SMALL_BOLD);
        badge.setForeground(FOAM);
        badge.setPreferredSize(new Dimension(28, 28));
        JPanel badgeWrap = new JPanel(new BorderLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge, BorderLayout.NORTH);

        JPanel t = new JPanel();
        t.setOpaque(false);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.add(left(label(title, BODY_BOLD, TEXT)));
        t.add(Box.createVerticalStrut(3));
        t.add(left(wrapped(text, SMALL, MUTED, 205)));

        p.add(badgeWrap, BorderLayout.WEST);
        p.add(t, BorderLayout.CENTER);
        return p;
    }
}
