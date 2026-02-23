package com.solvd.utils;

import java.util.Locale;

public final class TextUtils {

    private TextUtils() {
    }

    public static String normalizeText(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
