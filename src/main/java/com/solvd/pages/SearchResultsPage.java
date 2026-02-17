package com.solvd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;
import java.util.Locale;

public class SearchResultsPage extends AbstractPage {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#js-product-list");

    @FindBy(css = "#js-product-list")
    private WebElement productList;

    @FindBy(css = "#js-product-list .product-miniature")
    private List<WebElement> productCards;

    @FindBy(css = "#js-product-list .product-title a")
    private List<WebElement> productTitles;

    @FindBy(id = "product-search-no-matches")
    private List<WebElement> noMatchesHeader;

    @FindBy(css = "#content.page-content.page-not-found")
    private List<WebElement> pageNotFoundSection;

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }

    @Override
    protected ExpectedCondition<?> getPageLoadedCondition() {
        return d -> {
            try {
                return (productList != null && productList.isDisplayed())
                        || (productCards != null && !productCards.isEmpty())
                        || (productTitles != null && !productTitles.isEmpty())
                        || (noMatchesHeader != null && !noMatchesHeader.isEmpty())
                        || (pageNotFoundSection != null && !pageNotFoundSection.isEmpty());
            } catch (Exception e) {
                return false;
            }
        };
    }

    public boolean isDisplayed() {
        ensureLoaded();
        try {
            wait.until(d -> productList.isDisplayed());
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return isNoMatchesMessageDisplayed();
        }
    }

    public boolean hasAnyProductTitleContaining(String keyword) {
        ensureLoaded();
        String k = keyword.toLowerCase(Locale.ROOT);

        return productTitles.stream()
                .map(WebElement::getText)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .anyMatch(t -> t.contains(k));
    }

    public boolean isNoMatchesMessageDisplayed() {
        ensureLoaded();
        wait.until(d -> !noMatchesHeader.isEmpty() || !pageNotFoundSection.isEmpty() || !productCards.isEmpty());
        return noMatchesHeader.stream().anyMatch(WebElement::isDisplayed)
                || pageNotFoundSection.stream().anyMatch(WebElement::isDisplayed);
    }

    public int getVisibleProductCardCount() {
        ensureLoaded();
        wait.until(d -> productCards.stream().anyMatch(WebElement::isDisplayed)
                || noMatchesHeader.stream().anyMatch(WebElement::isDisplayed)
                || pageNotFoundSection.stream().anyMatch(WebElement::isDisplayed));

        if (isNoMatchesMessageDisplayed()) return 0;

        int retries = 2;
        for (int i = 0; i < retries; i++) {
            try {
                return (int) productCards.stream().filter(WebElement::isDisplayed).count();
            } catch (StaleElementReferenceException e) {
                if (i == retries - 1) throw e;
            }
        }
        return 0;
    }

    public ProductPage openFirstDisplayedProduct() {
        ensureLoaded();
        WebElement first = findFirstVisibleProductTitle();
        click(first, "findFirstVisibleProductTitle");
        return new ProductPage(driver);
    }

    public String getFirstDisplayedProductTitle() {
        ensureLoaded();
        return findFirstVisibleProductTitle().getText().trim();
    }

    public List<String> getVisibleProductTitles() {
        ensureLoaded();
        wait.until(d -> productTitles != null && !productTitles.isEmpty());
        return productTitles.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private WebElement findFirstVisibleProductTitle() {
        return wait.until(driver ->
                productTitles.stream()
                        .filter(e -> {
                            try {
                                return e.isDisplayed();
                            } catch (StaleElementReferenceException ex) {
                                return false;
                            }
                        })
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No visible product titles found"))
        );
    }
}
