package com.solvd.pages.common;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

public final class PageFactory {

    private static final String COMMON_PKG = ".pages.common.";
    private static final String ANDROID_PKG = ".pages.android.";
    private static final String DESKTOP_PKG = ".pages.desktop.";

    private PageFactory() {
    }

    public static <T> T initPage(WebDriver driver, Class<T> baseClass) {
        String implClassName = resolveImplClassName(driver, baseClass);

        try {
            Class<?> impl = Class.forName(implClassName);
            Object instance = impl.getConstructor(WebDriver.class).newInstance(driver);
            return baseClass.cast(instance);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Page impl not found: " + implClassName +
                            ". Check package/name and that src/main/java is on classpath.",
                    e
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot init page implementation: " + implClassName + " for base: " + baseClass.getName(),
                    e
            );
        }
    }

    private static <T> String resolveImplClassName(WebDriver driver, Class<T> baseClass) {
        boolean android = isAndroid(driver);

        String name = baseClass.getName();

        if (!name.contains(COMMON_PKG)) {
            throw new IllegalArgumentException("Base page must be in " + COMMON_PKG + ": " + name);
        }

        name = name.replace(COMMON_PKG, android ? ANDROID_PKG : DESKTOP_PKG);

        if (name.endsWith("Base")) {
            name = name.substring(0, name.length() - "Base".length());
        }

        return name;
    }

    private static boolean isAndroid(WebDriver driver) {
        if (!(driver instanceof HasCapabilities)) return false;

        Capabilities caps = ((HasCapabilities) driver).getCapabilities();

        Object platform = caps.getCapability("platform");
        if (platform != null && platform.toString().equalsIgnoreCase("android")) return true;

        Object platformName = caps.getCapability("platformName");
        return platformName != null && platformName.toString().equalsIgnoreCase("android");
    }
}