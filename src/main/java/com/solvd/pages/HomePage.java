package com.solvd.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HomePage extends AbstractPage {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#search_widget input[name='s']");

    @FindBy(css = "#search_widget input[name='s']")
    private WebElement searchInput;

    @FindBy(css = "#search_widget button[type='submit']")
    private WebElement searchSubmitButton;

    @FindBy(css = "#content .product-title a")
    private List<WebElement> productTitleLinks;

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void waitUntilElementLoaded() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        wait.until(d -> searchInput.isDisplayed());
    }

    public SearchResultsPage search(String query) {
        waitUntilElementLoaded();

        click(searchInput, "searchInput");
        sendKeys(searchInput, "searchInput",
                Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE, query);

        if (!clickIfPresent(searchSubmitButton, "searchSubmitButton")) {
            sendKeys(searchInput, "searchInput", Keys.ENTER);
        }

        return new SearchResultsPage(driver);
    }

    public String getSearchKeywordFromHome() {
        waitUntilElementLoaded();

        wait.until(d -> !productTitleLinks.isEmpty()
                && !productTitleLinks.get(0).getText().trim().isEmpty());

        String title = productTitleLinks.get(0).getText().trim();

        String[] tokens = title.split("[^A-Za-z0-9]+");
        for (String t : tokens) {
            if (t.length() >= 4) return t.toLowerCase();
        }
        return title.substring(0, Math.min(6, title.length())).toLowerCase();
    }

    public ProductPage openFirstProduct() {
        waitUntilElementLoaded();

        wait.until(d -> !productTitleLinks.isEmpty());
        WebElement first = productTitleLinks.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No displayed home product"));

        click(first, "firstHomeProduct");
        return new ProductPage(driver);
    }
}
