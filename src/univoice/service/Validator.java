package univoice.service;

import java.util.regex.Pattern;

public final class Validator {

    public static final int MAX_MESSAGE = 500;
    private static final Pattern SR_CODE = Pattern.compile("\\d{2}-\\d{5}");

    private Validator() { }

    public static boolean isValidSrCode(String srCode) {
        return srCode != null && SR_CODE.matcher(srCode.trim()).matches();
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
