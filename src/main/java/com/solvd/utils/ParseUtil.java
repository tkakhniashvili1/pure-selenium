package com.solvd.utils;

public final class ParseUtil {

    private ParseUtil() {
    }

    public static int parseCount(String raw) {
        if (raw == null || raw.isBlank()) return 0;

        int value = 0;
        boolean hasDigit = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') {
                hasDigit = true;
                value = value * 10 + (c - '0');
            }
        }

        return hasDigit ? value : 0;
    }
}
