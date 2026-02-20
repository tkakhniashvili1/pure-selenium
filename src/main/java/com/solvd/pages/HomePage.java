package com.solvd.pages;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HomePage extends BasePage {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#search_widget input[name='s']");

    @FindBy(css = "#search_widget input[name='s']")
    private ExtendedWebElement searchInput;

    @FindBy(css = "#search_widget button[type='submit']")
    private ExtendedWebElement searchSubmitButton;

    @FindBy(css = "#content .product-title a")
    private List<ExtendedWebElement> productTitleLinks;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(PAGE_READY_LOCATOR);
        searchInput.isElementPresent(getDefaultWaitTimeout());
    }

    public SearchResultsPage search(String query) {
        waitForPageOpened();

        searchInput.click();
        searchInput.getElement().clear();
        searchInput.type(query);

        if (searchSubmitButton.isPresent()) {
            searchSubmitButton.click();
        } else {
            searchInput.getElement().sendKeys(Keys.ENTER);
        }

        return new SearchResultsPage(getDriver());
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

    public ProductPage openFirstProduct() {
        waitForPageOpened();

        ExtendedWebElement first = productTitleLinks.stream()
                .filter(e -> e.isElementPresent(1))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No displayed home product"));

        if (!first.isElementPresent(getDefaultWaitTimeout())) {
            throw new NoSuchElementException("No displayed home product");
        }

        first.click();
        return new ProductPage(getDriver());
    }
}
