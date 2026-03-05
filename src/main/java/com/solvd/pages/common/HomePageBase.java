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
        if (!firstProductTitleLink.isElementPresent()) {
            throw new NoSuchElementException("No displayed home product");
        }

        firstProductTitleLink.click();
        return initPage(getDriver(), ProductPageBase.class);
    }

    public String getSearchKeywordFromHome() {
        if (!firstProductTitleLink.isElementPresent()) {
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