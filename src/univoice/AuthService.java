package univoice.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Signs people in and decides what they can open, based on the format of the
 * login ID:
 * <ul>
 *   <li>SR-Code (25-06680) &rarr; student account, student screens only</li>
 *   <li>ADM-#### (ADM-0001) &rarr; administrator account, admin screens only</li>
 * </ul>
 * Administrator accounts are private to this class. Student accounts are
 * created the first time an SR-Code signs in and are kept in a data file
 * (salted SHA-256, never the plain password).
 */
public class AuthService {

    public static final int MAX_ATTEMPTS = 3;
    public static final int LOCK_SECONDS = 30;

    /** ID -> SHA-256("ID:password"). The default ADM-0001 password is admin123. */
    private static final Map<String, String> ADMIN_HASHES;

    static {
        Map<String, String> admins = new HashMap<>();
        admins.put("ADM-0001", "f057dcf8a3db9a382984fdae30e7b89bbc0975a3803aeffd3a9129991402e59e");
        ADMIN_HASHES = Collections.unmodifiableMap(admins);
    }

    private final Path studentFile;
    private final Map<String, String[]> students = new LinkedHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private int attemptsLeft = MAX_ATTEMPTS;
    private long lockedUntil = 0;

    public AuthService(String studentFileName) {
        this.studentFile = Paths.get(studentFileName);
        loadStudents();
    }

    /** Thrown when a sign-in attempt is refused. The message is safe to show to the user. */
    public static class AuthException extends Exception {
        public AuthException(String message) {
            super(message);
        }
    }

    // ---------------------------------------------------------------- login

    /** True when this SR-Code has no account yet, so the next step is to create one. */
    public boolean needsRegistration(String loginId) {
        return Validator.detectRole(loginId) == Role.STUDENT && !students.containsKey(loginId.trim());
    }

    public Session login(String loginId, String password) throws AuthException {
        checkLock();
        String id = loginId == null ? "" : loginId.trim();
        Role role = Validator.detectRole(id);
        if (role == null) {
            throw new AuthException("Unrecognized ID format. Students use their SR-Code (e.g. 25-06680); "
                    + "administrators use their admin ID (e.g. ADM-0001).");
        }
        if (password == null || password.isEmpty()) {
            throw new AuthException("Please enter your password.");
        }

        boolean ok;
        if (role == Role.ADMIN) {
            id = id.toUpperCase();
            String expected = ADMIN_HASHES.get(id);
            ok = expected != null && expected.equals(sha256(id + ":" + password));
        } else {
            String[] saltAndHash = students.get(id);
            if (saltAndHash == null) {
                throw new AuthException("No student account for " + id + " yet. Create one first.");
            }
            ok = saltAndHash[1].equals(sha256(saltAndHash[0] + ":" + id + ":" + password));
        }

        if (!ok) {
            attemptsLeft--;
            if (attemptsLeft <= 0) {
                lockedUntil = System.currentTimeMillis() + LOCK_SECONDS * 1000L;
                attemptsLeft = MAX_ATTEMPTS;
                throw new AuthException("Too many failed attempts. Sign-in is locked for "
                        + LOCK_SECONDS + " seconds.");
            }
            throw new AuthException("Incorrect ID or password. Attempts left: " + attemptsLeft);
        }
        attemptsLeft = MAX_ATTEMPTS;
        return new Session(id, role);
    }

    /** Creates a student account for a new SR-Code and signs the student in. */
    public Session registerStudent(String srCode, String password) throws AuthException {
        checkLock();
        String id = srCode == null ? "" : srCode.trim();
        if (!Validator.isValidSrCode(id)) {
            throw new AuthException("Only SR-Codes (e.g. 25-06680) can create a student account.");
        }
        if (students.containsKey(id)) {
            throw new AuthException("An account for " + id + " already exists. Sign in instead.");
        }
        if (!Validator.isValidPassword(password)) {
            throw new AuthException("Use a password with at least " + Validator.MIN_PASSWORD + " characters.");
        }
        String salt = newSalt();
        students.put(id, new String[]{salt, sha256(salt + ":" + id + ":" + password)});
        appendStudent(id);
        attemptsLeft = MAX_ATTEMPTS;
        return new Session(id, Role.STUDENT);
    }

    public int getAttemptsLeft() { return attemptsLeft; }

    /** Seconds until sign-in unlocks, or 0 if it is not locked. */
    public int secondsLocked() {
        long ms = lockedUntil - System.currentTimeMillis();
        return ms <= 0 ? 0 : (int) Math.ceil(ms / 1000.0);
    }

    public boolean isLocked() { return secondsLocked() > 0; }

    private void checkLock() throws AuthException {
        int s = secondsLocked();
        if (s > 0) {
            throw new AuthException("Sign-in is locked. Try again in " + s + " seconds.");
        }
    }

    // ---------------------------------------------------------------- storage

    private void loadStudents() {
        students.clear();
        if (!Files.exists(studentFile)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(studentFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.trim().split("\\|");
                if (p.length == 3 && Validator.isValidSrCode(p[0])) {
                    students.put(p[0], new String[]{p[1], p[2]});
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + studentFile + ": " + e.getMessage());
        }
    }

    private void appendStudent(String id) {
        String[] sh = students.get(id);
        try {
            if (studentFile.getParent() != null) {
                Files.createDirectories(studentFile.getParent());
            }
            try (BufferedWriter w = Files.newBufferedWriter(studentFile, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(id + "|" + sh[0] + "|" + sh[1]);
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save " + studentFile + ": " + e.getMessage());
        }
    }

    private String newSalt() {
        byte[] b = new byte[8];
        random.nextBytes(b);
        return toHex(b);
    }

    private static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
