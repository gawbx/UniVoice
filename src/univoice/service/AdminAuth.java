package univoice.service;

public class AdminAuth {

    public static final int MAX_ATTEMPTS = 3;

    private final String password = "admin123";
    private int attemptsLeft = MAX_ATTEMPTS;

    public boolean login(String input) {
        if (isLocked()) {
            return false;
        }
        if (password.equals(input)) {
            attemptsLeft = MAX_ATTEMPTS;
            return true;
        }
        attemptsLeft--;
        return false;
    }

    public int getAttemptsLeft() { return attemptsLeft; }

    public boolean isLocked() { return attemptsLeft <= 0; }

    public void reset() { attemptsLeft = MAX_ATTEMPTS; }
}
