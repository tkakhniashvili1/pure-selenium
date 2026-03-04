package com.solvd.pages.common;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePage extends AbstractPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePage.class);
    private boolean frontOfficeIframeEnsured = false;

    @FindBy(css = "iframe#framelive, iframe.framelive, iframe[name='framelive']")
    private ExtendedWebElement frontOfficeIframe;

    public BasePage(WebDriver driver) {
        super(driver);
    }

    protected void ensureFrontOfficeIframe(ExtendedWebElement probeElement) {
        WebDriver driver = getDriver();
        openBaseUrlIfNeeded(driver);
        driver.switchTo().defaultContent();

        long timeout = getDefaultWaitTimeout().getSeconds();

        waitUntil(d -> {
            if (isAnyElementDisplayed(probeElement)) return true;

            if (frontOfficeIframe.isPresent()) {
                try {
                    d.switchTo().frame(frontOfficeIframe.getElement());
                    return isAnyElementDisplayed(probeElement);
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

        String currentUrl = null;
        try {
            currentUrl = driver.getCurrentUrl();
        } catch (WebDriverException e) {
            LOGGER.warn("Cannot get current URL, will navigate to base URL", e);
        }

        if (currentUrl == null || currentUrl.isBlank() || currentUrl.equals("about:blank") || currentUrl.startsWith("data:")) {
            driver.get(baseUrl);
        }
    }

    private boolean isAnyElementDisplayed(ExtendedWebElement... elements) {
        for (ExtendedWebElement element : elements) {
            if (element.isVisible()) {
                return true;
            }
        }
        return false;
    }

    protected final void ensureFrontOfficeIframeOnce(ExtendedWebElement probeElement) {
        if (frontOfficeIframeEnsured && probeElement != null && probeElement.isElementPresent(1)) {
            return;
        }

        ensureFrontOfficeIframe(probeElement);
        frontOfficeIframeEnsured = true;
    }
}