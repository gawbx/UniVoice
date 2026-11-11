package univoice;

import univoice.model.Complaint;
import univoice.model.Feedback;
import univoice.model.Status;
import univoice.model.Suggestion;
import univoice.service.AdminAuth;
import univoice.service.FeedbackManager;
import univoice.service.Validator;
import univoice.ui.MainFrame;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Scanner;

public class CampusTracker {

    private static final String[] DEPARTMENTS = {"CICS", "COE", "CAFAD", "BAET", "CET", "CHE"};
    public static final String DATA_FILE = "data/feedback.txt";

    private final Scanner scanner = new Scanner(System.in);
    private final AdminAuth auth = new AdminAuth();
    private final FeedbackManager manager = new FeedbackManager(DATA_FILE);

    public static String[] getDepartments() {
        return DEPARTMENTS.clone();
    }

    public static void main(String[] args) {
        boolean console = args.length > 0 && args[0].equalsIgnoreCase("--console");
        if (console) {
            new CampusTracker().runConsole();
        } else {
            System.setProperty("apple.awt.application.name", "UniVoice");
            SwingUtilities.invokeLater(() -> MainFrame.launch(new FeedbackManager(DATA_FILE), new AdminAuth()));
        }
    }

    private void runConsole() {
        System.out.println("=========================================");
        System.out.println("  UniVoice - Campus Feedback (console)");
        System.out.println("=========================================");
        boolean running = true;
        while (running) {
            String answer = read("\nAre you an administrator? (y/n, q to quit): ").toLowerCase();
            if (answer.equals("q")) {
                running = false;
            } else if (answer.equals("y")) {
                if (adminLogin()) {
                    adminMenu();
                }
            } else if (answer.equals("n")) {
                userMenu();
            } else {
                System.out.println("Please type y, n, or q.");
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

    private boolean adminLogin() {
        auth.reset();
        while (!auth.isLocked()) {
            if (auth.login(read("Admin password: "))) {
                System.out.println("Welcome, administrator.");
                return true;
            }
            System.out.println("Incorrect password. Attempts left: " + auth.getAttemptsLeft());
        }
        System.out.println("Too many failed attempts. Returning to the start.");
        auth.reset();
        return false;
    }

    private void userMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- User menu ---");
            System.out.println("1. Submit a suggestion or complaint");
            System.out.println("2. Back");
            String choice = read("Choice: ");
            if (choice.equals("1")) {
                submitFeedback();
            } else if (choice.equals("2")) {
                back = true;
            } else {
                System.out.println("Please choose 1 or 2.");
            }
        }
    }

    private void submitFeedback() {
        String srCode = askSRCode();
        String department = askDepartment();
        String type = askType();
        String message = askMessage();
        Feedback saved = manager.addFeedback(type, srCode, department, message);
        System.out.println("\nSaved as Pending. Your Record ID is " + saved.getRecordId() + " - keep it!");
    }

    private String askSRCode() {
        while (true) {
            String code = read("SR-Code (e.g. 25-06680): ");
            if (Validator.isValidSrCode(code)) {
                return code;
            }
            System.out.println("Invalid format. Use two digits, a dash, then five digits.");
        }
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
            System.out.println("\n--- Admin menu ---");
            System.out.println("1. View all records");
            System.out.println("2. View Pending records");
            System.out.println("3. View Resolved records");
            System.out.println("4. Search by SR-Code");
            System.out.println("5. Update a record's status");
            System.out.println("6. Delete a Resolved record");
            System.out.println("7. Logout");
            switch (read("Choice: ")) {
                case "1": printRecords(manager.listRecords(null)); break;
                case "2": printRecords(manager.listRecords(Status.PENDING)); break;
                case "3": printRecords(manager.listRecords(Status.RESOLVED)); break;
                case "4": printRecords(manager.searchBySrCode(read("SR-Code to search: "))); break;
                case "5": updateStatus(); break;
                case "6": deleteRecord(); break;
                case "7": logout = true; break;
                default: System.out.println("Please choose 1-7.");
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
}
