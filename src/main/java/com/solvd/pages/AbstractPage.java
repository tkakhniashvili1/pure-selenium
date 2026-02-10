package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

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

    protected void ensureFrontOfficeIframe(By probeBy) {
        try {
            if (isAnyDisplayed(probeBy)) return;
        } catch (WebDriverException ignored) {
        }

        driver.switchTo().defaultContent();

        wait.until(d -> !d.findElements(FRONT_OFFICE_IFRAME_BY).isEmpty());
        WebElement frame = driver.findElements(FRONT_OFFICE_IFRAME_BY).get(0);

        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
        wait.until(d -> isAnyDisplayed(probeBy));
    }

    private boolean isAnyDisplayed(By by) {
        List<WebElement> els = driver.findElements(by);
        for (WebElement el : els) {
            try {
                if (el != null && el.isDisplayed()) return true;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return false;
    }

    protected void click(WebElement element, String name) {
        log.debug("click: {}", name);
        wait.until(ExpectedConditions.visibilityOf(element));
        scrollIntoView(element);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (ElementClickInterceptedException e) {
            log.debug("click fallback(js): {} ({})", name, e.getClass().getSimpleName());
            jsClick(element);
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
        wait.until(ExpectedConditions.visibilityOf(element));
        element.sendKeys(keys);
    }

    protected String getText(WebElement element, String name) {
        wait.until(ExpectedConditions.visibilityOf(element));
        String t = element.getText();
        log.debug("getText: {} -> {}", name, t);
        return t == null ? "" : t.trim();
    }

    protected String getAttribute(WebElement element, String name, String attr) {
        wait.until(ExpectedConditions.visibilityOf(element));
        String v = element.getAttribute(attr);
        log.debug("getAttribute: {} [{}] -> {}", name, attr, v);
        return v;
    }

    protected String textContent(WebElement el) {
        Object v = ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", el);
        return v == null ? "" : v.toString().trim();
    }

    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element
            );
        } catch (JavascriptException ignored) {
        }
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
