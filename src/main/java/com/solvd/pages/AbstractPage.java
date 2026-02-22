package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class AbstractPage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final By FRONT_OFFICE_IFRAME_BY = By.cssSelector("iframe#framelive");

    protected AbstractPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait")))
        );
        PageFactory.initElements(driver, this);
    }

    protected void switchToFrontOfficeFrameIfNeeded(By probeBy) {
        try {
            if (isAnyElementDisplayed(probeBy)) return;
        } catch (WebDriverException e) {
            log.debug("Initial probe visibility check failed: {}", e.getMessage());
        }

        driver.switchTo().defaultContent();

        WebElement frame = wait.until(ExpectedConditions.presenceOfElementLocated(FRONT_OFFICE_IFRAME_BY));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));

        wait.until(ExpectedConditions.visibilityOfElementLocated(probeBy));
    }

    private boolean isAnyElementDisplayed(By by) {
        return driver.findElements(by).stream()
                .anyMatch(el -> {
                    try {
                        return el.isDisplayed();
                    } catch (StaleElementReferenceException ignored) {
                        return false;
                    }
                });
    }

    protected void click(WebElement element, String name) {
        log.debug("click: {}", name);

        wait.until(ExpectedConditions.elementToBeClickable(element));
        scrollIntoView(element);

        try {
            element.click();
        } catch (ElementNotInteractableException e) {
            log.debug("click fallback(js): {} ({})", name, e.getClass().getSimpleName());
            jsClick(element);
        }
    }

    protected boolean clickIfPresent(WebElement element, String name) {
        try {
            click(element, name);
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            log.debug("clickIfPresent skipped: {} ({})", name, e.getClass().getSimpleName());
            return false;
        }
    }

    protected void sendKeys(WebElement element, String name, CharSequence... keys) {
        log.debug("sendKeys: {}", name);
        waitUntilVisible(element);
        element.sendKeys(keys);
    }

    protected String getText(WebElement element, String elementName) {
        waitUntilVisible(element);
        String text = element.getText();
        log.debug("getText from element {}: {}", elementName, text);
        return text == null ? "" : text.trim();
    }

    protected String getAttribute(WebElement element, String elementName, String attribute) {
        waitUntilVisible(element);
        String value = element.getAttribute(attribute);
        log.debug("getAttribute [{}] from element {}: {}", attribute, elementName, value);
        return value;
    }

    protected String getTextContent(WebElement element) {
        Object value = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].textContent;", element);
        String text = value == null ? "" : value.toString().trim();
        log.debug("getTextContent: {}", text);
        return text;
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element
            );
            log.debug("Scrolled element into view: {}", element);
        } catch (JavascriptException e) {
            log.warn("Failed to scroll element into view: {}", e.getMessage());
        }
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private void waitUntilVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }
}
