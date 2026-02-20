package com.solvd.utils;

public final class ParseUtil {

    private ParseUtil() {
    }

    public static int parseCount(String raw) {
        if (raw == null) return 0;

        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;

        return Integer.parseInt(digits);
    }
}
