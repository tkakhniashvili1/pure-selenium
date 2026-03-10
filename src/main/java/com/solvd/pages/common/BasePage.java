package com.solvd.pages.common;

import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;
import org.openqa.selenium.WebDriver;

public abstract class BasePage extends AbstractPage {

    private boolean frontOfficeIframeEnsured = false;

    public BasePage(WebDriver driver) {
        super(driver);
    }

    protected final void ensureFrontOfficeIframe(ExtendedWebElement probeElement) {
        if (probeElement != null && probeElement.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) {
            return;
        }

        driver.switchTo().defaultContent();
        driver.switchTo().frame("frontOfficeFrame");
    }

    protected final void ensureFrontOfficeIframeOnce(ExtendedWebElement probeElement) {
        if (frontOfficeIframeEnsured && probeElement != null && probeElement.isElementPresent(1)) {
            return;
        }

        ensureFrontOfficeIframe(probeElement);
        frontOfficeIframeEnsured = true;
    }
}