package univoice;

import univoice.model.Complaint;
import univoice.model.Feedback;
import univoice.model.Status;
import univoice.model.Suggestion;
import univoice.service.AuthService;
import univoice.service.FeedbackManager;
import univoice.service.Session;
import univoice.service.Validator;
import univoice.ui.MainFrame;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Scanner;

public class CampusTracker {

    private static final String[] DEPARTMENTS = {"CICS", "COE", "CAFAD", "BAET", "CET", "CHE"};
    public static final String DATA_FILE = "data/feedback.txt";
    public static final String STUDENT_FILE = "data/students.txt";

    private final Scanner scanner = new Scanner(System.in);
    private final AuthService auth = new AuthService(STUDENT_FILE);
    private final FeedbackManager manager = new FeedbackManager(DATA_FILE);
    private Session session;

    public static String[] getDepartments() {
        return DEPARTMENTS.clone();
    }

    public static void main(String[] args) {
        boolean console = args.length > 0 && args[0].equalsIgnoreCase("--console");
        if (console) {
            new CampusTracker().runConsole();
        } else {
            System.setProperty("apple.awt.application.name", "UniVoice");
            SwingUtilities.invokeLater(() -> MainFrame.launch(new FeedbackManager(DATA_FILE), new AuthService(STUDENT_FILE)));
        }
    }

    private void runConsole() {
        System.out.println("=========================================");
        System.out.println("  UniVoice - Campus Feedback (console)");
        System.out.println("=========================================");
        System.out.println("Sign in with your SR-Code (students, e.g. 25-06680)");
        System.out.println("or your administrator ID (admins, e.g. ADM-0001).");
        boolean running = true;
        while (running) {
            String id = read("\nLogin ID (q to quit): ");
            if (id.equalsIgnoreCase("q")) {
                running = confirm("Exit UniVoice? (y/n): ") ? false : true;
                continue;
            }
            if (login(id)) {
                if (session.isAdmin()) {
                    adminMenu();
                } else {
                    userMenu();
                }
                session = null;
            }
        }
        System.out.println("Goodbye!");
    }

    private String read(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private boolean confirm(String prompt) {
        return read(prompt).equalsIgnoreCase("y");
    }

    /** Signs in by ID format: SR-Code = student only, ADM-#### = administrator only. */
    private boolean login(String id) {
        if (Validator.detectRole(id) == null) {
            System.out.println("Unrecognized format. Use an SR-Code (25-06680) or an admin ID (ADM-0001).");
            return false;
        }
        try {
            if (auth.needsRegistration(id)) {
                System.out.println("No account yet for " + id + ". Let's create one.");
                String pw = read("New password (min " + Validator.MIN_PASSWORD + " characters): ");
                if (!pw.equals(read("Confirm password: "))) {
                    System.out.println("The passwords don't match.");
                    return false;
                }
                if (!confirm("Create a student account for " + id + "? (y/n): ")) {
                    System.out.println("Cancelled.");
                    return false;
                }
                session = auth.registerStudent(id, pw);
            } else {
                session = auth.login(id, read("Password: "));
            }
            System.out.println("Welcome, " + session.getUserId() + " (" + session.getRole().getLabel() + ").");
            return true;
        } catch (AuthService.AuthException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void userMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Student menu (" + session.getUserId() + ") ---");
            System.out.println("1. Submit a suggestion or complaint");
            System.out.println("2. Log out");
            String choice = read("Choice: ");
            if (choice.equals("1")) {
                submitFeedback();
            } else if (choice.equals("2")) {
                back = confirm("Log out? (y/n): ");
            } else {
                System.out.println("Please choose 1 or 2.");
            }
        }
    }

    private void submitFeedback() {
        String srCode = session.getUserId();
        System.out.println("SR-Code: " + srCode);
        String department = askDepartment();
        String type = askType();
        String message = askMessage();
        if (!confirm("Submit this " + type.toLowerCase() + "? It can't be edited afterwards (y/n): ")) {
            System.out.println("Not submitted.");
            return;
        }
        Feedback saved = manager.addFeedback(type, srCode, department, message);
        System.out.println("\nSaved as Pending. Your Record ID is " + saved.getRecordId() + " - keep it!");
    }

    private String askDepartment() {
        while (true) {
            System.out.println("Select a department:");
            for (int i = 0; i < DEPARTMENTS.length; i++) {
                System.out.println("  " + (i + 1) + ". " + DEPARTMENTS[i]);
            }
            try {
                int choice = Integer.parseInt(read("Choice: "));
                if (choice >= 1 && choice <= DEPARTMENTS.length) {
                    return DEPARTMENTS[choice - 1];
                }
                System.out.println("Please choose a number from the list.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private String askType() {
        while (true) {
            System.out.println("What are you sending?\n  1. Suggestion\n  2. Complaint");
            String choice = read("Choice: ");
            if (choice.equals("1")) {
                return Suggestion.TYPE;
            } else if (choice.equals("2")) {
                return Complaint.TYPE;
            }
            System.out.println("Please choose 1 or 2.");
        }
    }

    private String askMessage() {
        while (true) {
            String message = read("Your message (max " + Validator.MAX_MESSAGE + " characters): ");
            if (Validator.isValidMessage(message)) {
                return message;
            }
            System.out.println("The message cannot be empty or longer than " + Validator.MAX_MESSAGE + " characters.");
        }
    }

    private void adminMenu() {
        boolean logout = false;
        while (!logout) {
            System.out.println("\n--- Admin menu (" + session.getUserId() + ") ---");
            System.out.println("1. View all records");
            System.out.println("2. View Pending records");
            System.out.println("3. View Resolved records");
            System.out.println("4. Search by SR-Code");
            System.out.println("5. Update a record's status");
            System.out.println("6. Delete a Resolved record");
            System.out.println("7. Remove a troll / spam record");
            System.out.println("8. Log out");
            switch (read("Choice: ")) {
                case "1": printRecords(manager.listRecords(null)); break;
                case "2": printRecords(manager.listRecords(Status.PENDING)); break;
                case "3": printRecords(manager.listRecords(Status.RESOLVED)); break;
                case "4": printRecords(manager.searchBySrCode(read("SR-Code to search: "))); break;
                case "5": updateStatus(); break;
                case "6": deleteRecord(); break;
                case "7": removeTroll(); break;
                case "8": logout = confirm("Log out? (y/n): "); break;
                default: System.out.println("Please choose 1-8.");
            }
        }
    }

    private void printRecords(List<Feedback> list) {
        if (list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Feedback f : list) {
            System.out.println("\n-----------------------------------------");
            System.out.println(f.display());
        }
        System.out.println("-----------------------------------------");
        System.out.println(list.size() + " record(s).");
    }

    private int askRecordId() {
        while (true) {
            try {
                return Validator.parseRecordId(read("Record ID (e.g. UV-0003 or 3): "));
            } catch (NumberFormatException e) {
                System.out.println("That is not a valid Record ID.");
            }
        }
    }

    private void updateStatus() {
        Feedback f = manager.findById(askRecordId());
        if (f == null) {
            System.out.println("No record with that ID.");
            return;
        }
        System.out.println(f.getRecordId() + " is currently " + f.getStatus().getLabel() + ".");
        String choice = read("Set to: 1. Pending  2. Resolved: ");
        if ((choice.equals("1") || choice.equals("2")) && !confirm("Change the status of " + f.getRecordId() + "? (y/n): ")) {
            System.out.println("No change made.");
            return;
        }
        if (choice.equals("1")) {
            manager.updateStatus(f.getId(), Status.PENDING);
            System.out.println("Marked as Pending.");
        } else if (choice.equals("2")) {
            manager.updateStatus(f.getId(), Status.RESOLVED);
            System.out.println("Marked as Resolved.");
        } else {
            System.out.println("No change made.");
        }
    }

    private void deleteRecord() {
        Feedback f = manager.findById(askRecordId());
        if (f == null) {
            System.out.println("No record with that ID.");
            return;
        }
        if (!f.isResolved()) {
            System.out.println("Only Resolved records can be deleted.");
            return;
        }
        if (read("Delete " + f.getRecordId() + "? This cannot be undone (y/n): ").equalsIgnoreCase("y")) {
            manager.deleteRecord(f.getId());
            System.out.println("Record deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private void removeTroll() {
        Feedback f = manager.findById(askRecordId());
        if (f == null) {
            System.out.println("No record with that ID.");
            return;
        }
        System.out.println(f.display());
        String reason = read("Reason (e.g. troll, spam, offensive): ");
        if (reason.isEmpty()) {
            reason = "Troll / spam";
        }
        String typed = read("Type " + f.getRecordId() + " to remove it permanently: ");
        if (!typed.equalsIgnoreCase(f.getRecordId())) {
            System.out.println("Removal cancelled.");
            return;
        }
        manager.removeInvalidRecord(f.getId(), reason, session.getUserId());
        System.out.println(f.getRecordId() + " removed and logged.");
    }
}
