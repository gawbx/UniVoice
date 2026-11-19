package univoice.ui;

import univoice.service.AuthService;
import univoice.service.FeedbackManager;
import univoice.service.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static univoice.ui.Theme.*;

/**
 * Main window. It shows the sign-in page until someone logs in, then builds
 * only the screens that person's role is allowed to see:
 * students get the feedback form (light mode), administrators get the
 * dashboard (dark mode). Both can open "About us".
 */
public class MainFrame extends JFrame {

    private static final Dimension PHONE = new Dimension(412, 860);

    private final FeedbackManager manager;
    private final AuthService auth;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private final Map<String, String> navLabels = new LinkedHashMap<>();
    private final List<RoundedButton> navButtons = new ArrayList<>();
    private JPanel header;
    private JPanel footer;
    private Session session;
    private String current = "";
    private boolean compact = false;
    private boolean mobileView = false;
    private Rectangle desktopBounds;

    public static void launch(FeedbackManager manager, AuthService auth) {
        Theme.installLookAndFeel();
        new MainFrame(manager, auth).setVisible(true);
    }

    private MainFrame(FeedbackManager manager, AuthService auth) {
        super("UniVoice - Campus Feedback");
        this.manager = manager;
        this.auth = auth;

        setIconImage(Theme.logo(128));
        try {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(Theme.logo(256));
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {
            // Not every platform lets apps set the taskbar icon.
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { checkCompact(); }
        });

        setSize(1180, 820);
        setMinimumSize(new Dimension(360, 600));
        setLocationRelativeTo(null);
        rebuild();
    }

    // ------------------------------------------------------------ building

    /** Rebuilds every screen for the current session and theme. */
    private void rebuild() {
        MenuSelectionManager.defaultManager().clearSelectedPath();
        Theme.apply(session != null && session.isAdmin() ? Mode.DARK : Mode.LIGHT);
        content.removeAll();
        pages.clear();
        navLabels.clear();

        if (session == null) {
            addPage("login", "Sign in", new LoginPanel(auth, this::onLogin));
        } else if (session.isStudent()) {
            addPage("home", "Home", new SubmitPanel(manager, session.getUserId()));
        } else {
            addPage("dashboard", "Dashboard", new AdminPanel(manager, session.getUserId()));
        }
        addPage("about", "About us", new AboutPanel());

        content.setBackground(BG);
        getContentPane().removeAll();
        getContentPane().setBackground(BG);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(content, BorderLayout.CENTER);
        compact = getContentPane().getWidth() > 0
                ? getContentPane().getWidth() < Responsive.COMPACT_BELOW
                : getWidth() < Responsive.COMPACT_BELOW;
        applyCompact();
        show(pages.keySet().iterator().next());
    }

    private void addPage(String key, String navLabel, JComponent page) {
        pages.put(key, page);
        navLabels.put(key, navLabel);
        content.add(page, key);
    }

    private void applyCompact() {
        if (header != null) {
            getContentPane().remove(header);
        }
        if (footer != null) {
            getContentPane().remove(footer);
        }
        header = buildHeader();
        footer = buildFooter();
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(footer, BorderLayout.SOUTH);
        for (JComponent page : pages.values()) {
            if (page instanceof Responsive) {
                ((Responsive) page).setCompact(compact);
            }
        }
        highlightNav();
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private void checkCompact() {
        int w = getContentPane().getWidth();
        boolean nowCompact = w > 0 && w < Responsive.COMPACT_BELOW;
        if (nowCompact != compact) {
            compact = nowCompact;
            applyCompact();
        }
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(12, 0));
        h.setBackground(HEADER_BG);
        h.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 3, 0, HEADER_LINE),
                new EmptyBorder(compact ? 10 : 12, compact ? 14 : 26, compact ? 10 : 12, compact ? 12 : 26)));

        JPanel brand = transparent(new BorderLayout(10, 0));
        brand.add(new JLabel(new ImageIcon(Theme.logo(compact ? 34 : 40))), BorderLayout.WEST);
        JPanel names = vstack();
        names.add(Box.createVerticalGlue());
        names.add(label("UniVoice", new Font(HEADING.getFamily(), Font.BOLD, compact ? 17 : 19), HEADER_TEXT));
        names.add(label(compact ? "BatStateU Alangilan" : "Campus feedback  ·  BatStateU Alangilan", SMALL, HEADER_MUTED));
        names.add(Box.createVerticalGlue());
        brand.add(names, BorderLayout.CENTER);
        h.add(brand, BorderLayout.WEST);

        navButtons.clear();
        JPanel right = transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        if (compact) {
            RoundedButton menu = new RoundedButton("Menu", RoundedButton.Style.NAV_OUTLINE);
            menu.addActionListener(e -> showMenu(menu));
            right.add(menu);
        } else {
            for (Map.Entry<String, String> item : navLabels.entrySet()) {
                RoundedButton b = new RoundedButton(item.getValue(), RoundedButton.Style.NAV);
                b.putClientProperty("page", item.getKey());
                b.addActionListener(e -> show(item.getKey()));
                navButtons.add(b);
                right.add(b);
            }
            if (session != null) {
                right.add(Box.createHorizontalStrut(10));
                right.add(userChip());
                RoundedButton logout = new RoundedButton("Log out", RoundedButton.Style.NAV_OUTLINE);
                logout.addActionListener(e -> confirmLogout());
                right.add(logout);
            }
        }
        JPanel rightWrap = transparent(new GridBagLayout());
        rightWrap.add(right);
        h.add(rightWrap, BorderLayout.EAST);
        return h;
    }

    private JComponent userChip() {
        JPanel chip = vstack();
        JLabel id = label(session.getUserId(), SMALL_BOLD, HEADER_TEXT);
        JLabel role = label(session.getRole().getLabel(), CAPTION, HEADER_MUTED);
        id.setAlignmentX(Component.RIGHT_ALIGNMENT);
        role.setAlignmentX(Component.RIGHT_ALIGNMENT);
        chip.add(id);
        chip.add(role);
        chip.setBorder(new EmptyBorder(0, 4, 0, 8));
        return chip;
    }

    private void showMenu(Component anchor) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(6, 0, 6, 0)));
        if (session != null) {
            JMenuItem who = new JMenuItem(session.getUserId() + "  ·  " + session.getRole().getLabel());
            who.setEnabled(false);
            who.setFont(SMALL_BOLD);
            menu.add(styled(who));
            menu.addSeparator();
        }
        for (Map.Entry<String, String> item : navLabels.entrySet()) {
            String text = (item.getKey().equals(current) ? "•  " : "    ") + item.getValue();
            JMenuItem mi = styled(new JMenuItem(text));
            mi.addActionListener(e -> { menu.setVisible(false); show(item.getKey()); });
            menu.add(mi);
        }
        menu.addSeparator();
        JMenuItem view = styled(new JMenuItem("    " + (mobileView ? "Switch to desktop view" : "Switch to mobile view")));
        view.addActionListener(e -> { menu.setVisible(false); toggleMobileView(); });
        menu.add(view);
        if (session != null) {
            JMenuItem logout = styled(new JMenuItem("    Log out"));
            logout.setForeground(DANGER);
            logout.addActionListener(e -> { menu.setVisible(false); confirmLogout(); });
            menu.add(logout);
        }
        menu.show(anchor, anchor.getWidth() - 230, anchor.getHeight() + 6);
    }

    private static JMenuItem styled(JMenuItem mi) {
        mi.setBackground(SURFACE);
        if (mi.isEnabled()) {
            mi.setForeground(TEXT);
        }
        mi.setFont(mi.getFont() == null || mi.getFont().getSize() > 12 ? BODY_BOLD : mi.getFont());
        mi.setBorder(new EmptyBorder(10, 14, 10, 18));
        mi.setPreferredSize(new Dimension(230, mi.getPreferredSize().height));
        return mi;
    }

    private JPanel buildFooter() {
        JPanel f = new JPanel(new BorderLayout(12, 0));
        f.setBackground(FOOTER_BG);
        f.setBorder(new EmptyBorder(compact ? 8 : 10, compact ? 14 : 26, compact ? 8 : 10, compact ? 14 : 26));
        if (compact) {
            f.add(label("BatStateU · CICS Alangilan", SMALL, FOOTER_TEXT), BorderLayout.WEST);
        } else {
            JPanel left = transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
            left.add(label("Batangas State University – Alangilan  ·  College of Informatics and Computing Sciences   ",
                    SMALL, FOOTER_TEXT));
            left.add(label("Leading Innovations, Transforming Lives, Building the Nation", SMALL_BOLD,
                    isDark() ? PRIMARY : new Color(0xF2A7B3)));
            f.add(left, BorderLayout.CENTER);
        }
        JButton toggle = new JButton(mobileView ? "Desktop view" : "Mobile view");
        toggle.setFont(SMALL_BOLD);
        toggle.setForeground(FOOTER_TEXT);
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setBorder(new EmptyBorder(0, 0, 0, 0));
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setToolTipText("Preview the compact layout used on narrow, phone-sized windows");
        toggle.addActionListener(e -> toggleMobileView());
        f.add(toggle, BorderLayout.EAST);
        return f;
    }

    // ------------------------------------------------------------ navigation

    private void show(String key) {
        if (!pages.containsKey(key)) {
            return;
        }
        if (pages.get(key) instanceof AdminPanel) {
            ((AdminPanel) pages.get(key)).refresh();
        }
        current = key;
        cards.show(content, key);
        highlightNav();
    }

    private void highlightNav() {
        for (RoundedButton b : navButtons) {
            b.setActive(current.equals(b.getClientProperty("page")));
        }
    }

    private void toggleMobileView() {
        if (!mobileView) {
            desktopBounds = getBounds();
            if ((getExtendedState() & MAXIMIZED_BOTH) != 0) {
                setExtendedState(NORMAL);
            }
            setSize(PHONE);
            setLocationRelativeTo(null);
            mobileView = true;
        } else {
            mobileView = false;
            if (desktopBounds != null && desktopBounds.width >= Responsive.COMPACT_BELOW) {
                setBounds(desktopBounds);
            } else {
                setSize(1180, 820);
                setLocationRelativeTo(null);
            }
        }
        checkCompact();
        applyCompact();
    }

    // ------------------------------------------------------------ session

    private void onLogin(Session s) {
        session = s;
        rebuild();
    }

    private void confirmLogout() {
        if (session == null) {
            return;
        }
        String msg = "You'll return to the sign-in page.";
        JComponent home = pages.get("home");
        if (home instanceof SubmitPanel && ((SubmitPanel) home).hasDraft()) {
            msg = "You have feedback you haven't submitted yet. It will be discarded if you log out.";
        }
        if (Dialogs.confirm(this, Dialogs.Kind.WARNING, "Log out of UniVoice?", msg, "Log out", "Stay signed in")) {
            session = null;
            rebuild();
        }
    }

    private void confirmExit() {
        String msg = session == null
                ? "Close the UniVoice app?"
                : "You're signed in as " + session.getUserId() + ". Closing the app will also log you out.";
        JComponent home = pages.get("home");
        if (home instanceof SubmitPanel && ((SubmitPanel) home).hasDraft()) {
            msg = "You have feedback you haven't submitted yet. It will be lost if you exit now.";
        }
        if (Dialogs.confirm(this, Dialogs.Kind.WARNING, "Exit UniVoice?", msg, "Exit", "Cancel")) {
            dispose();
            System.exit(0);
        }
    }
}
