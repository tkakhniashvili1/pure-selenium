package com.solvd.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UiActions {

    private static final Logger log = LoggerFactory.getLogger(UiActions.class);

    private UiActions() {
    }

    public static void click(WebDriver driver, WebDriverWait wait, WebElement element) {
        log.debug("UiActions.click: attempt");

        wait.until(ExpectedConditions.visibilityOf(element));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element
        );
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
            log.debug("UiActions.click: success");
        } catch (ElementNotInteractableException e) {
            log.debug("UiActions.click: ElementNotInteractable -> JS click");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    public static boolean clickIfPresent(WebDriver driver, WebDriverWait wait, WebElement element) {
        try {
            click(driver, wait, element);
            log.debug("UiActions.clickIfPresent: clicked");
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            log.debug("UiActions.clickIfPresent: not clicked ({})", e.getClass().getSimpleName());
            return false;
        }
    }

    public static String normalizeText(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase(java.util.Locale.ROOT);
    }
}
