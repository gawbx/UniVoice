package univoice.service;

import java.util.regex.Pattern;

public final class Validator {

    public static final int MAX_MESSAGE = 500;
    public static final int MIN_PASSWORD = 6;

    /** Student login format: the SR-Code, two digits, a dash, five digits (25-06680). */
    private static final Pattern SR_CODE = Pattern.compile("\\d{2}-\\d{5}");
    /** Administrator login format: ADM, a dash, four digits (ADM-0001). */
    private static final Pattern ADMIN_ID = Pattern.compile("(?i)ADM-\\d{4}");

    private Validator() { }

    public static boolean isValidSrCode(String srCode) {
        return srCode != null && SR_CODE.matcher(srCode.trim()).matches();
    }

    public static boolean isValidAdminId(String adminId) {
        return adminId != null && ADMIN_ID.matcher(adminId.trim()).matches();
    }

    /**
     * Works out the role from the format of the login ID alone.
     *
     * @return STUDENT for an SR-Code, ADMIN for an ADM-#### ID, or null if it matches neither
     */
    public static Role detectRole(String loginId) {
        if (isValidSrCode(loginId)) {
            return Role.STUDENT;
        }
        if (isValidAdminId(loginId)) {
            return Role.ADMIN;
        }
        return null;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD;
    }

    public static boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty() && message.trim().length() <= MAX_MESSAGE;
    }

    public static int parseRecordId(String input) {
        String text = input.trim().toUpperCase();
        if (text.startsWith("UV-")) {
            text = text.substring(3);
        }
        return Integer.parseInt(text);
    }
}
