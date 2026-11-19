package univoice.ui;

import univoice.CampusTracker;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static univoice.ui.Theme.*;

/**
 * "About us": a short description of UniVoice and the developers.
 * Photos are read from the assets/developers folder; when a photo is missing
 * the card shows the person's initials instead.
 */
public class AboutPanel extends JPanel implements Responsive {

    /** Folder (relative to the project root) where the team's photos go. */
    public static final String PHOTO_FOLDER = "assets/developers";

    private static final class Developer {
        final String name, role, srCode, github, bio, photo, initials;

        Developer(String name, String role, String srCode, String github, String bio, String photo, String initials) {
            this.name = name;
            this.role = role;
            this.srCode = srCode;
            this.github = github;
            this.bio = bio;
            this.photo = photo;
            this.initials = initials;
        }
    }

    private static final Developer[] TEAM = {
            new Developer("Gabb Nickolai D. Año", "Project lead", "25-06680", "gawbx",
                    "Designed the Feedback, Suggestion, and Complaint classes (inheritance, polymorphism, "
                            + "abstraction) and maintains the GitHub repository.", "ano", "GA"),
            new Developer("Neorwin Symone B. Cruzat", "Records & admin features", "25-04041", "neorwinsymone-del",
                    "Built FeedbackManager: record storage, file saving and loading, and the administrator "
                            + "tools to view, search, update, and delete records.", "cruzat", "NC"),
            new Developer("Nico Elijah B. Dimayacyac", "Main program & testing", "25-06814", "Nico-Dimayacyac",
                    "Built the main program, sign-in, menus, and input validation with exception handling; "
                            + "leads testing and debugging.", "dimayacyac", "ND"),
    };

    private final Page page;
    private final JLabel heading = label("The Team behind UniVoice", HEADING, TEXT);

    public AboutPanel() {
        JPanel col = vstack();
        col.add(left(caption("About us")));
        col.add(Box.createVerticalStrut(4));
        col.add(left(heading));
        col.add(Box.createVerticalStrut(6));
        col.add(wrapped("UniVoice is a campus feedback system for Batangas State University – Alangilan. "
                + "Students send suggestions and complaints in one place, and administrators get an organized "
                + "list they can review, resolve, and clean up.", BODY, MUTED));
        col.add(Box.createVerticalStrut(22));

        col.add(projectCard());
        col.add(Box.createVerticalStrut(28));

        col.add(left(label("Meet the developers", SUBHEAD, TEXT)));
        col.add(Box.createVerticalStrut(4));
        col.add(left(label("Team 12  ·  BeeSahCool  ·  BSIT-2107", SMALL_BOLD, MUTED)));
        col.add(Box.createVerticalStrut(14));

        JPanel grid = transparent(new GridFlowLayout(230, 3, 16, 16));
        for (Developer d : TEAM) {
            grid.add(devCard(d));
        }
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(grid);
        col.add(Box.createVerticalStrut(18));

        page = new Page(col, 980, false);
        setLayout(new BorderLayout());
        add(page.inScroll(), BorderLayout.CENTER);
    }

    private JPanel projectCard() {
        RoundedPanel card = RoundedPanel.card().withAccent(PRIMARY);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 24, 22, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(wrapped("UniVoice: Empowering Campus Progress, One Voice at a Time", BODY_BOLD, TEXT));
        card.add(Box.createVerticalStrut(14));

        JPanel facts = transparent(new GridFlowLayout(190, 4, 16, 14));
        facts.add(fact("Course", "OOP 101 – Object-Oriented Programming"));
        facts.add(fact("College", "CICS, Alangilan Campus"));
        facts.add(fact("Instructor", "Ms. Fatima Marie P. Agdon, MSCS"));
        facts.add(fact("School year", "1st Semester, AY 2026-2027"));
        facts.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(facts);
        return card;
    }

    private static JPanel fact(String name, String value) {
        JPanel p = vstack();
        p.add(left(caption(name)));
        p.add(Box.createVerticalStrut(4));
        p.add(wrapped(value, BODY_BOLD, TEXT));
        return p;
    }

    private JPanel devCard(Developer d) {
        RoundedPanel card = RoundedPanel.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        card.add(left(new Avatar(loadPhoto(d.photo), d.initials, 84)));
        card.add(Box.createVerticalStrut(14));
        card.add(wrapped(d.name, SUBHEAD, TEXT));
        card.add(Box.createVerticalStrut(2));
        card.add(left(label(d.role.toUpperCase(), CAPTION, PRIMARY)));
        card.add(Box.createVerticalStrut(12));

        JPanel meta = transparent(new GridFlowLayout(140, 2, 10, 10));
        meta.add(fact("SR-Code", d.srCode));
        meta.add(fact("GitHub", "@" + d.github));
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(meta);
        card.add(Box.createVerticalStrut(12));
        card.add(wrapped(d.bio, SMALL, MUTED));
        return card;
    }

    // ---------------------------------------------------------------- photos

    /** Finds assets/developers whether the app runs from the project folder or elsewhere. */
    static Path photoFolder() {
        Path direct = Paths.get(PHOTO_FOLDER);
        if (direct.toFile().isDirectory()) {
            return direct;
        }
        try {
            Path classes = Paths.get(CampusTracker.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path fromBuild = classes.getParent() == null ? null : classes.getParent().resolve(PHOTO_FOLDER);
            if (fromBuild != null && fromBuild.toFile().isDirectory()) {
                return fromBuild;
            }
        } catch (URISyntaxException | SecurityException | NullPointerException ignored) {
            // Fall back to the relative path.
        }
        return direct;
    }

    private static BufferedImage loadPhoto(String baseName) {
        Path folder = photoFolder();
        for (String ext : new String[]{".png", ".jpg", ".jpeg", ".PNG", ".JPG", ".JPEG"}) {
            File f = folder.resolve(baseName + ext).toFile();
            if (f.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        return img;
                    }
                } catch (IOException e) {
                    System.err.println("Could not read photo " + f + ": " + e.getMessage());
                }
            }
        }
        return null;
    }

    /** Round photo, or initials on a soft red circle when there is no photo yet. */
    private static final class Avatar extends JComponent {
        private final BufferedImage photo;
        private final String initials;
        private final int size;

        Avatar(BufferedImage photo, String initials, int size) {
            this.photo = photo == null ? null : squareCrop(photo, size * 2);
            this.initials = initials;
            this.size = size;
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        private static BufferedImage squareCrop(BufferedImage src, int target) {
            int s = Math.min(src.getWidth(), src.getHeight());
            int x = (src.getWidth() - s) / 2;
            int y = Math.max(0, (src.getHeight() - s) / 3);
            BufferedImage out = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, target, target, x, y, x + s, y + s, null);
            g.dispose();
            return out;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Ellipse2D circle = new Ellipse2D.Float(0, 0, size - 1, size - 1);
            if (photo != null) {
                g2.setClip(circle);
                g2.drawImage(photo, 0, 0, size, size, null);
                g2.setClip(null);
            } else {
                g2.setColor(PRIMARY_SOFT);
                g2.fill(circle);
                g2.setColor(PRIMARY);
                g2.setFont(new Font(HEADING.getFamily(), Font.BOLD, size / 3));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (size - fm.stringWidth(initials)) / 2, (size - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.setColor(LINE);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Float(1, 1, size - 3, size - 3));
            g2.dispose();
        }
    }

    @Override
    public void setCompact(boolean compact) {
        heading.setFont(compact ? SUBHEAD : HEADING);
        page.setPadding(compact ? 14 : 32, compact ? 16 : 28);
        revalidate();
    }
}
