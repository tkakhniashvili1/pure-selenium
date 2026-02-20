package com.solvd.utils;

import java.util.Locale;

public final class UiActions {

    private UiActions() {
    }

    public static String normalizeText(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
