package univoice.model;

public enum Status {
    PENDING("Pending"),
    RESOLVED("Resolved");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static Status fromString(String text) {
        return (text != null && text.trim().equalsIgnoreCase("RESOLVED")) ? RESOLVED : PENDING;
    }
}
