package univoice.model;

import java.time.LocalDateTime;

public class Complaint extends Feedback {

    public static final String TYPE = "Complaint";

    public Complaint(int id, String srCode, String department, String message) {
        this(id, srCode, department, message, LocalDateTime.now(), Status.PENDING);
    }

    public Complaint(int id, String srCode, String department, String message,
                     LocalDateTime dateFiled, Status status) {
        super(id, srCode, department, TYPE, message, dateFiled, status);
    }

    public String getUrgencyNote() {
        return isResolved() ? "Resolved - thank you for reporting." : "Needs attention from the administrators.";
    }

    @Override
    public String display() {
        return "COMPLAINT - a problem that needs fixing\n" + commonDisplay() + "\n\nNote: " + getUrgencyNote();
    }

    @Override
    public String toFileLine() {
        return "COMPLAINT|" + commonFields();
    }
}
