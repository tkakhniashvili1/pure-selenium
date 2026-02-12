package com.solvd.tests;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_DIR = "screenshots/";

    static {
        createScreenshotDirectory();
    }

    private static void createScreenshotDirectory() {
        try {
            Path path = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                LOGGER.info("Created screenshot directory: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create screenshot directory", e);
        }
    }

    public static void captureScreenshot(WebDriver driver, String testName, String browser) {
        if (driver == null) {
            LOGGER.warn("Cannot capture screenshot: driver is null");
            return;
        }

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s_%s.png", testName, browser, timestamp);
            Path filepath = Paths.get(SCREENSHOT_DIR, filename);

            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), filepath);

            LOGGER.info("Screenshot saved: {}", filepath.toAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Failed to capture screenshot for test: {}", testName, e);
        }
    }
}