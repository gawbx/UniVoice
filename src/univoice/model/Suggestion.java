package univoice.model;

import java.time.LocalDateTime;

public class Suggestion extends Feedback {

    public static final String TYPE = "Suggestion";

    public Suggestion(int id, String srCode, String department, String message) {
        this(id, srCode, department, message, LocalDateTime.now(), Status.PENDING);
    }

    public Suggestion(int id, String srCode, String department, String message,
                      LocalDateTime dateFiled, Status status) {
        super(id, srCode, department, TYPE, message, dateFiled, status);
    }

    @Override
    public String display() {
        return "SUGGESTION - an idea to improve campus\n" + commonDisplay();
    }

    @Override
    public String toFileLine() {
        return "SUGGESTION|" + commonFields();
    }
}
