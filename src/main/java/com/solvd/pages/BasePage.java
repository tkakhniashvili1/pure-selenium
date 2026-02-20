package com.solvd.pages;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public abstract class BasePage extends AbstractPage {

    @FindBy(css = "iframe#framelive, iframe.framelive, iframe[name='framelive']")
    private ExtendedWebElement frontOfficeIframe;

    private boolean frontOfficeIframeEnsured = false;

    public BasePage(WebDriver driver) {
        super(driver);
    }

    protected void ensureFrontOfficeIframe(By probeBy) {
        WebDriver driver = getDriver();
        openBaseUrlIfNeeded(driver);
        driver.switchTo().defaultContent();

        long timeout = getDefaultWaitTimeout().getSeconds();

        waitUntil(d -> {
            if (isAnyElementDisplayed(d, probeBy)) return true;

            if (frontOfficeIframe.isPresent()) {
                try {
                    d.switchTo().frame(frontOfficeIframe.getElement());
                    return isAnyElementDisplayed(d, probeBy);
                } catch (StaleElementReferenceException e) {
                    d.switchTo().defaultContent();
                    return false;
                }
            }

            return false;
        }, timeout);
    }

    private void openBaseUrlIfNeeded(WebDriver driver) {
        String baseUrl = Configuration.getRequired("url");

        String currentUrl;
        try {
            currentUrl = driver.getCurrentUrl();
        } catch (Exception e) {
            currentUrl = null;
        }

        if (currentUrl == null || currentUrl.isBlank() || currentUrl.equals("about:blank") || currentUrl.startsWith("data:")) {
            driver.get(baseUrl);
        }
    }

    private boolean isAnyElementDisplayed(WebDriver driver, By by) {
        try {
            for (WebElement element : driver.findElements(by)) {
                try {
                    if (element != null && element.isDisplayed()) return true;
                } catch (StaleElementReferenceException ignored) {
                }
            }
        } catch (WebDriverException ignored) {
        }
        return false;
    }

    protected final void ensureFrontOfficeIframeOnce(By probeBy) {
        if (frontOfficeIframeEnsured && (isInsideIframe() || isAnyElementDisplayed(getDriver(), probeBy))) {
            return;
        }

        ensureFrontOfficeIframe(probeBy);
        frontOfficeIframeEnsured = true;
    }

    private boolean isInsideIframe() {
        try {
            return Boolean.TRUE.equals(((JavascriptExecutor) getDriver())
                    .executeScript("return window.self !== window.top;"));
        } catch (Exception e) {
            return false;
        }
    }
}
