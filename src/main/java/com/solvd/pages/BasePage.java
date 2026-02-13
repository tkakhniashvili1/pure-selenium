package com.solvd.pages;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.webdriver.gui.AbstractPage;
import org.openqa.selenium.*;

public abstract class BasePage extends AbstractPage {

    private static final By FRONT_OFFICE_IFRAME_BY =
            By.cssSelector("iframe#framelive, iframe.framelive, iframe[name='framelive']");

    public BasePage(WebDriver driver) {
        super(driver);
    }

    protected void ensureFrontOfficeIframe(By probeBy) {
        WebDriver d = getDriver();

        openBaseUrlIfNeeded(d);

        d.switchTo().defaultContent();

        if (isAnyElementDisplayed(d, probeBy)) return;

        long timeout = getDefaultWaitTimeout().getSeconds();

        waitUntil(driver ->
                        isAnyElementDisplayed(driver, probeBy) ||
                                !driver.findElements(FRONT_OFFICE_IFRAME_BY).isEmpty(),
                timeout
        );

        if (isAnyElementDisplayed(d, probeBy)) return;

        waitUntil(driver -> {
            try {
                WebElement iframe = driver.findElement(FRONT_OFFICE_IFRAME_BY);
                driver.switchTo().frame(iframe);
                return true;
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                driver.switchTo().defaultContent();
                return false;
            }
        }, timeout);

        waitUntil(driver -> isAnyElementDisplayed(driver, probeBy), timeout);
    }

    private void openBaseUrlIfNeeded(WebDriver d) {
        String baseUrl = Configuration.getRequired("url");

        String cur;
        try {
            cur = d.getCurrentUrl();
        } catch (Exception e) {
            cur = null;
        }

        if (cur == null || cur.isBlank() || cur.equals("about:blank") || cur.startsWith("data:")) {
            d.get(baseUrl);
        }
    }

    private boolean isAnyElementDisplayed(WebDriver d, By by) {
        try {
            for (WebElement el : d.findElements(by)) {
                try {
                    if (el != null && el.isDisplayed()) return true;
                } catch (StaleElementReferenceException ignored) {
                }
            }
        } catch (WebDriverException ignored) {
        }
        return false;
    }
}
