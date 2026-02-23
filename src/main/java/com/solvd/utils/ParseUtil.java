package com.solvd.utils;

public final class ParseUtil {

    private ParseUtil() {
    }

    public static int parseCount(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }

        int value = 0;
        for (char c : raw.toCharArray()) {
            if (Character.isDigit(c)) {
                value = value * 10 + (c - '0');
            }
        }

        return value;
    }
}
