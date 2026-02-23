package com.solvd.pages.common;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.webdriver.gui.AbstractPage;
import org.openqa.selenium.*;

public abstract class BasePage extends AbstractPage {

    private static final By FRONT_OFFICE_IFRAME_BY =
            By.cssSelector("iframe#framelive, iframe.framelive, iframe[name='framelive']");

    private boolean frontOfficeIframeEnsured = false;

    public BasePage(WebDriver driver) {
        super(driver);
    }

    protected void ensureFrontOfficeIframe(By probeBy) {
        WebDriver driver = getDriver();

        openBaseUrlIfNeeded(driver);

        driver.switchTo().defaultContent();

        if (isAnyElementDisplayed(driver, probeBy)) return;

        long timeout = getDefaultWaitTimeout().getSeconds();

        waitUntil(d ->
                        isAnyElementDisplayed(d, probeBy) ||
                                !d.findElements(FRONT_OFFICE_IFRAME_BY).isEmpty(),
                timeout
        );

        if (isAnyElementDisplayed(driver, probeBy)) return;

        waitUntil(d -> {
            try {
                WebElement iframe = d.findElement(FRONT_OFFICE_IFRAME_BY);
                d.switchTo().frame(iframe);
                return true;
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                d.switchTo().defaultContent();
                return false;
            }
        }, timeout);

        waitUntil(d -> isAnyElementDisplayed(d, probeBy), timeout);
    }

//    private void openBaseUrlIfNeeded(WebDriver driver) {
//        String baseUrl = Configuration.getRequired("url");
//
//        String currentUrl;
//        try {
//            currentUrl = driver.getCurrentUrl();
//        } catch (Exception e) {
//            currentUrl = null;
//        }
//
//        if (currentUrl == null
//                || currentUrl.isBlank()
//                || currentUrl.equals("about:blank")
//                || currentUrl.startsWith("data:")
//                || currentUrl.startsWith("chrome://")
//                || currentUrl.startsWith("chrome-search://")) {
//            driver.get(baseUrl);
//        }
//    }

    private void openBaseUrlIfNeeded(WebDriver driver) {
        String baseUrl = Configuration.getRequired("url");

        String currentUrl;
        try {
            currentUrl = driver.getCurrentUrl();
        } catch (Exception e) {
            currentUrl = null;
        }

        boolean needsNavigation =
                currentUrl == null ||
                        currentUrl.isBlank() ||
                        !(currentUrl.startsWith("http://") || currentUrl.startsWith("https://"));

        if (needsNavigation) {
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
