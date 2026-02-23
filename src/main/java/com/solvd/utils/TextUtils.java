package com.solvd.utils;

import java.util.Locale;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private TextUtils() {
    }

    public static String normalizeText(String s) {
        if (s == null) return "";
        return MULTI_SPACE.matcher(s.trim())
                .replaceAll(" ")
                .toLowerCase(Locale.ROOT);
    }
}