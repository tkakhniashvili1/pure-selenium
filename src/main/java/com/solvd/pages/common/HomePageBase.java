package com.solvd.pages.common;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public abstract class HomePageBase extends BasePage {

    @FindBy(css = "#search_widget input[name='s']")
    private ExtendedWebElement searchInput;

    @FindBy(css = "#search_widget button[type='submit']")
    private ExtendedWebElement searchSubmitButton;

    @FindBy(css = "#content .product-title a")
    private List<ExtendedWebElement> productTitleLinks;

    public HomePageBase(WebDriver driver) {
        super(driver);
    }

    protected abstract By getPageReadyLocator();

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(getPageReadyLocator());
        searchInput.isElementPresent(getDefaultWaitTimeout());
    }

    public SearchResultsPageBase search(String query) {
        waitForPageOpened();

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
        waitForPageOpened();

        ExtendedWebElement first = productTitleLinks.stream()
                .filter(e -> e.isElementPresent(1))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No product titles"));

        if (!first.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No product titles");
        }

        String title = first.getText().trim();

        String[] tokens = title.split("[^A-Za-z0-9]+");
        for (String t : tokens) {
            if (t.length() >= 4) return t.toLowerCase();
        }
        return title.substring(0, Math.min(6, title.length())).toLowerCase();
    }

    public ProductPageBase openFirstProduct() {
        waitForPageOpened();

        ExtendedWebElement first = productTitleLinks.stream()
                .filter(e -> e.isElementPresent(1))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No displayed home product"));

        if (!first.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No displayed home product");
        }

        first.click();
        return initPage(getDriver(), ProductPageBase.class);
    }
}
