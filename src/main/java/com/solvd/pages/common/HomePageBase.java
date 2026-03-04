/* package com.solvd.pages.common;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.support.FindBy;

import java.util.Set;

public abstract class HomePageBase extends BasePage {

    @FindBy(css = "#search_widget input[name='s']")
    private ExtendedWebElement searchInput;

    @FindBy(css = "#search_widget button[type='submit']")
    private ExtendedWebElement searchSubmitButton;

    @FindBy(css = "#content .product-title a")
    private ExtendedWebElement firstProductTitleLink;

    public HomePageBase(WebDriver driver) {
        super(driver);
        waitForPageOpened();
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(searchInput);
        searchInput.isElementPresent(getDefaultWaitTimeout());
    }

    public SearchResultsPageBase search(String query) {
        searchInput.click();
        searchInput.getElement().clear();
        searchInput.type(query);

        if (searchSubmitButton.isPresent()) {
            searchSubmitButton.click();
        } else {
            searchInput.getElement().sendKeys(Keys.ENTER);
        }

        return initPage(getDriver(), SearchResultsPageBase.class);
    }

    public String getSearchKeywordFromHome() {
        if (!firstProductTitleLink.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No product titles");
        }
        String title = firstProductTitleLink.getText().trim();

        String[] tokens = title.split("[^A-Za-z0-9]+");
        for (String t : tokens) {
            if (t.length() >= 4) return t.toLowerCase();
        }
        return title.substring(0, Math.min(6, title.length())).toLowerCase();
    }

    public ProductPageBase openFirstProduct() {
        waitForPageOpened();

        if (!firstProductTitleLink.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No displayed home product");
        }

        firstProductTitleLink.click();
        return initPage(getDriver(), ProductPageBase.class);
    }
    public String getCurrentContext() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        return driver.getContext();
    }


    public void switchBackToWeb() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        for (String context : driver.getContextHandles()) {
            String uppercaseContext = context.toUpperCase();
            if (uppercaseContext.contains("CHROMIUM") || uppercaseContext.contains("WEBVIEW")) {
                driver.context(context);
                return;
            }
        }
        throw new RuntimeException("No WEBVIEW/CHROMIUM context found: " + driver.getContextHandles());
    }

    private WebDriver unwrap(WebDriver driver) {
        WebDriver unwrappedDriver = driver;
        while (unwrappedDriver instanceof WrapsDriver) {
            unwrappedDriver = ((WrapsDriver) unwrappedDriver).getWrappedDriver();
        }
        return unwrappedDriver;
    }
}
*/

package com.solvd.pages.common;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

import java.util.Set;

public abstract class HomePageBase extends BasePage {

    @FindBy(css = "#search_widget input[name='s']")
    private ExtendedWebElement searchInput;

    @FindBy(css = "#search_widget button[type='submit']")
    private ExtendedWebElement searchSubmitButton;

    @FindBy(css = "#content .product-title a")
    private ExtendedWebElement firstProductTitleLink;

    public HomePageBase(WebDriver driver) {
        super(driver);
        ensureFrontOfficeIframeOnce(searchInput);
        setUiLoadedMarker(searchInput);
    }

    public SearchResultsPageBase search(String query) {
        searchInput.click();
        searchInput.getElement().clear();
        searchInput.type(query);

        if (searchSubmitButton.isPresent()) {
            searchSubmitButton.click();
        } else {
            searchInput.getElement().sendKeys(Keys.ENTER);
        }

        return initPage(getDriver(), SearchResultsPageBase.class);
    }

    public ProductPageBase openFirstProduct() {
        if (!firstProductTitleLink.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No displayed home product");
        }

        firstProductTitleLink.click();
        return initPage(getDriver(), ProductPageBase.class);
    }

    public Set<String> getAvailableContexts() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        return driver.getContextHandles();
    }

    public void handleNativePopup() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());

        driver.context("NATIVE_APP");
        waitUntil(d -> "NATIVE_APP".equals(driver.getContext()), getDefaultWaitTimeout());

        driver.navigate().back();
    }

    private WebDriver unwrap(WebDriver driver) {
        WebDriver unwrappedDriver = driver;
        while (unwrappedDriver instanceof WrapsDriver) {
            unwrappedDriver = ((WrapsDriver) unwrappedDriver).getWrappedDriver();
        }
        return unwrappedDriver;
    }

    public void switchBackToWeb() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        for (String context : driver.getContextHandles()) {
            String uppercaseContext = context.toUpperCase();
            if (uppercaseContext.contains("CHROMIUM") || uppercaseContext.contains("WEBVIEW")) {
                driver.context(context);
                return;
            }
        }
        throw new RuntimeException("No WEBVIEW/CHROMIUM context found: " + driver.getContextHandles());
    }

    public String getCurrentContext() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        return driver.getContext();
    }

    public String getSearchKeywordFromHome() {
        if (!firstProductTitleLink.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No product titles");
        }
        String title = firstProductTitleLink.getText().trim();

        String[] tokens = title.split("[^A-Za-z0-9]+");
        for (String t : tokens) {
            if (t.length() >= 4) return t.toLowerCase();
        }
        return title.substring(0, Math.min(6, title.length())).toLowerCase();
    }

    public void triggerNativeRequiredActionInWeb() {
        String baseUrl = Configuration.getRequired("url");

        WebDriver driver = getDriver();
        driver.switchTo().defaultContent();

        if (!(driver instanceof JavascriptExecutor js)) {
            throw new IllegalStateException("Driver does not support JavaScript execution: " + driver.getClass());
        }

        js.executeScript("window.open(arguments[0], '_blank');", baseUrl);
    }
}