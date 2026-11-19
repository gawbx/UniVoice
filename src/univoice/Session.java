package univoice.service;

/** An immutable record of the person currently signed in. */
public final class Session {

    private final String userId;
    private final Role role;

    Session(String userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public Role getRole() { return role; }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isStudent() { return role == Role.STUDENT; }
}
