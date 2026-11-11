package univoice.model;

import java.time.LocalDateTime;

public final class FeedbackFactory {

    private FeedbackFactory() { }

    public static Feedback create(String type, int id, String srCode, String department, String message) {
        if (Complaint.TYPE.equalsIgnoreCase(type)) {
            return new Complaint(id, srCode, department, message);
        }
        return new Suggestion(id, srCode, department, message);
    }

    public static Feedback fromFileLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 7) {
            throw new IllegalArgumentException("Malformed record: " + line);
        }
        int id = Integer.parseInt(p[1]);
        Status status = Status.fromString(p[4]);
        LocalDateTime date = LocalDateTime.parse(p[5], Feedback.DATE_FORMAT);
        String message = Feedback.unescape(p[6]);

        if ("COMPLAINT".equalsIgnoreCase(p[0])) {
            return new Complaint(id, p[2], p[3], message, date, status);
        }
        return new Suggestion(id, p[2], p[3], message, date, status);
    }
}
