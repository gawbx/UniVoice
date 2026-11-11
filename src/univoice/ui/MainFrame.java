package univoice.ui;

import univoice.service.AdminAuth;
import univoice.service.FeedbackManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static univoice.ui.Theme.*;

public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final RoundedButton homeBtn = new RoundedButton("Home", RoundedButton.Style.NAV);
    private final RoundedButton sendBtn = new RoundedButton("Send feedback", RoundedButton.Style.NAV);
    private final RoundedButton adminBtn = new RoundedButton("Admin sign in", RoundedButton.Style.NAV);
    private final SubmitPanel submitPanel;
    private final AdminLoginPanel loginPanel;
    private final AdminPanel adminPanel;
    private boolean adminLoggedIn = false;
    private String current = "";

    public static void launch(FeedbackManager manager, AdminAuth auth) {
        Theme.install();
        new MainFrame(manager, auth).setVisible(true);
    }

    private MainFrame(FeedbackManager manager, AdminAuth auth) {
        super("UniVoice - Campus Feedback");

        submitPanel = new SubmitPanel(manager, () -> show("home"));
        loginPanel = new AdminLoginPanel(auth, this::onLoginSuccess, () -> show("home"));
        adminPanel = new AdminPanel(manager, this::onLogout);

        content.setBackground(CREAM);
        content.add(new HomePanel(() -> show("send"), this::openAdmin), "home");
        content.add(submitPanel, "send");
        content.add(loginPanel, "login");
        content.add(adminPanel, "admin");

        homeBtn.addActionListener(e -> show("home"));
        sendBtn.addActionListener(e -> show("send"));
        adminBtn.addActionListener(e -> openAdmin());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(), BorderLayout.NORTH);
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(buildFooter(), BorderLayout.SOUTH);

        setIconImage(Theme.logo(128));
        try {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(Theme.logo(256));
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {

        }

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 800);
        setMinimumSize(new Dimension(980, 700));
        setLocationRelativeTo(null);
        show("home");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ESPRESSO);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 3, 0, CARAMEL), new EmptyBorder(12, 28, 12, 28)));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new JLabel(new ImageIcon(Theme.logo(44))), BorderLayout.WEST);
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(Box.createVerticalGlue());
        names.add(label("UniVoice", new Font("Georgia", Font.BOLD, 20), FOAM));
        names.add(label("Campus feedback", SMALL, LATTE));
        names.add(Box.createVerticalGlue());
        brand.add(names, BorderLayout.CENTER);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));
        nav.add(homeBtn);
        nav.add(Box.createHorizontalStrut(6));
        nav.add(sendBtn);
        nav.add(Box.createHorizontalStrut(6));
        nav.add(adminBtn);
        JPanel navWrap = new JPanel(new GridBagLayout());
        navWrap.setOpaque(false);
        navWrap.add(nav);

        header.add(brand, BorderLayout.WEST);
        header.add(navWrap, BorderLayout.EAST);
        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
        footer.setBackground(ESPRESSO);
        footer.add(label("Batangas State University - Alangilan Campus  |  College of Informatics and Computing Sciences", SMALL, LATTE));
        footer.add(label("Leading Innovations, Transforming Lives, Building the Nation", SMALL_BOLD, CARAMEL));
        return footer;
    }

    private void show(String key) {
        if ("send".equals(key) && !"send".equals(current)) {
            submitPanel.reset();
        }
        if ("login".equals(key)) {
            loginPanel.reset();
        }
        if ("admin".equals(key)) {
            adminPanel.refresh();
        }
        current = key;
        cards.show(content, key);
        homeBtn.setActive("home".equals(key));
        sendBtn.setActive("send".equals(key));
        adminBtn.setActive("login".equals(key) || "admin".equals(key));
    }

    private void openAdmin() {
        show(adminLoggedIn ? "admin" : "login");
    }

    private void onLoginSuccess() {
        adminLoggedIn = true;
        adminBtn.setText("Dashboard");
        show("admin");
    }

    private void onLogout() {
        adminLoggedIn = false;
        adminBtn.setText("Admin sign in");
        show("home");
    }
}
