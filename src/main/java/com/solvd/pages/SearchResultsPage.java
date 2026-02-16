package com.solvd.pages;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Locale;

public class SearchResultsPage extends BasePage {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#js-product-list");

    @FindBy(css = "#js-product-list")
    private ExtendedWebElement productList;

    @FindBy(css = "#js-product-list .product-miniature")
    private List<ExtendedWebElement> productCards;

    @FindBy(css = "#js-product-list .product-title a")
    private List<ExtendedWebElement> productTitles;

    @FindBy(id = "product-search-no-matches")
    private List<ExtendedWebElement> noMatchesHeader;

    @FindBy(css = "#content.page-content.page-not-found")
    private List<ExtendedWebElement> pageNotFoundSection;

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDisplayed() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        return productList.isElementPresent(getDefaultWaitTimeout()) || isNoMatchesMessageDisplayed();
    }

    public boolean hasAnyProductTitleContaining(String keyword) {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        String k = keyword.toLowerCase(Locale.ROOT);

        return productTitles.stream()
                .map(ExtendedWebElement::getText)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .anyMatch(t -> t.contains(k));
    }

    public boolean isNoMatchesMessageDisplayed() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        waitUntil(d -> !noMatchesHeader.isEmpty() || !pageNotFoundSection.isEmpty() || !productCards.isEmpty(), getDefaultWaitTimeout());
        return noMatchesHeader.stream().anyMatch(ExtendedWebElement::isDisplayed)
                || pageNotFoundSection.stream().anyMatch(ExtendedWebElement::isDisplayed);
    }

    public int getVisibleProductCardCount() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        waitUntil(d -> productCards.stream().anyMatch(ExtendedWebElement::isDisplayed)
                || noMatchesHeader.stream().anyMatch(ExtendedWebElement::isDisplayed)
                || pageNotFoundSection.stream().anyMatch(ExtendedWebElement::isDisplayed), getDefaultWaitTimeout());

        if (isNoMatchesMessageDisplayed()) return 0;

        int retries = 2;
        for (int i = 0; i < retries; i++) {
            try {
                return (int) productCards.stream().filter(ExtendedWebElement::isDisplayed).count();
            } catch (StaleElementReferenceException e) {
                if (i == retries - 1) throw e;
            }
        }
        return 0;
    }

    public ProductPage openFirstDisplayedProduct() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        ExtendedWebElement first = firstVisibleProductTitle();
        first.click();
        return new ProductPage(getDriver());
    }

    public String getFirstDisplayedProductTitle() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        return firstVisibleProductTitle().getText().trim();
    }

    public List<String> getDisplayedProductTitles() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        waitUntil(d -> productTitles != null && !productTitles.isEmpty(), getDefaultWaitTimeout());
        return productTitles.stream()
                .filter(ExtendedWebElement::isDisplayed)
                .map(ExtendedWebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private ExtendedWebElement firstVisibleProductTitle() {
        waitUntil(d -> productTitles.stream().anyMatch(ExtendedWebElement::isDisplayed), getDefaultWaitTimeout());
        return productTitles.stream()
                .filter(ExtendedWebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible product titles found"));
    }
}
