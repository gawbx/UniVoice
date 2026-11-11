package univoice.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Feedback {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final int id;
    private final String srCode;
    private final String department;
    private final String type;
    private final String message;
    private final LocalDateTime dateFiled;
    private Status status;

    protected Feedback(int id, String srCode, String department, String type,
                       String message, LocalDateTime dateFiled, Status status) {
        this.id = id;
        this.srCode = srCode;
        this.department = department;
        this.type = type;
        this.message = message;
        this.dateFiled = dateFiled;
        this.status = (status == null) ? Status.PENDING : status;
    }

    public int getId() { return id; }
    public String getRecordId() { return String.format("UV-%04d", id); }
    public String getSrCode() { return srCode; }
    public String getDepartment() { return department; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public LocalDateTime getDateFiled() { return dateFiled; }
    public String getDateFiledText() { return dateFiled.format(DATE_FORMAT); }
    public Status getStatus() { return status; }
    public boolean isResolved() { return status == Status.RESOLVED; }

    public void setStatus(Status newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public abstract String display();

    public abstract String toFileLine();

    protected String commonDisplay() {
        return "Record ID: " + getRecordId()
                + "\nSR-Code: " + srCode
                + "\nDepartment: " + department
                + "\nStatus: " + status.getLabel()
                + "\nDate filed: " + getDateFiledText()
                + "\n\nMessage:\n" + message;
    }

    protected String commonFields() {
        return id + "|" + srCode + "|" + department + "|" + status.name()
                + "|" + getDateFiledText() + "|" + escape(message);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("|", "\\p").replace("\r", "").replace("\n", "\\n");
    }

    static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                sb.append(next == 'n' ? '\n' : next == 'p' ? '|' : next);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
