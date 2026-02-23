package com.solvd.utils;

import java.math.BigDecimal;

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

    public static int parseIntegerFromText(String raw) {
        if (raw == null) return 0;
        String digits = raw.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public static BigDecimal parseMoney(String raw) {
        if (raw == null) return BigDecimal.ZERO;

        String s = raw.replaceAll("[^0-9,\\.]", "");

        long commas = s.chars().filter(ch -> ch == ',').count();
        if (commas == 1 && s.indexOf('.') == -1) s = s.replace(',', '.');
        else s = s.replace(",", "");

        return s.isBlank() ? BigDecimal.ZERO : new BigDecimal(s);
    }
}
