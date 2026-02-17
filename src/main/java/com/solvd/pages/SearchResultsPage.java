package com.solvd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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

    public boolean isDisplayed() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        try {
            wait.until(d -> productList.isDisplayed());
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return isNoMatchesMessageDisplayed();
        }
    }

    public boolean hasAnyProductTitleContaining(String keyword) {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        String k = keyword.toLowerCase(Locale.ROOT);

        return productTitles.stream()
                .map(WebElement::getText)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .anyMatch(t -> t.contains(k));
    }

    public boolean isNoMatchesMessageDisplayed() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        wait.until(d -> !noMatchesHeader.isEmpty() || !pageNotFoundSection.isEmpty() || !productCards.isEmpty());
        return noMatchesHeader.stream().anyMatch(WebElement::isDisplayed)
                || pageNotFoundSection.stream().anyMatch(WebElement::isDisplayed);
    }

    public int getVisibleProductCardCount() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
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

    public ProductPage openFirstVisibleProduct() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        WebElement first = firstVisibleProductTitle();
        click(first, "firstVisibleProductTitle");
        return new ProductPage(driver);
    }

    public String getFirstVisibleProductTitle() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        return firstVisibleProductTitle().getText().trim();
    }

    public List<String> getDisplayedProductTitles() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        wait.until(d -> productTitles != null && !productTitles.isEmpty());
        return productTitles.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private WebElement firstVisibleProductTitle() {
        wait.until(d -> productTitles.stream().anyMatch(WebElement::isDisplayed));
        return productTitles.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible product titles found"));
    }
}
