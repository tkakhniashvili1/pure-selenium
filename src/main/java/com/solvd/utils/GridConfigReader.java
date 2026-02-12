package com.solvd.utils;

import java.io.InputStream;
import java.util.Properties;

public class GridConfigReader {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = GridConfigReader.class.getClassLoader()
                .getResourceAsStream("grid-config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                properties.setProperty("grid.url", "http://localhost:4444");
                properties.setProperty("use.grid", "false");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static boolean useGrid() {
        return Boolean.parseBoolean(properties.getProperty("use.grid", "false"));
    }
}