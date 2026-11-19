package univoice.service;

/** Who is signed in. The role decides which screens and theme the app shows. */
public enum Role {
    STUDENT("Student"),
    ADMIN("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
