package com.solvd.pages;

<<<<<<< HEAD
import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
=======
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.*;
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Locale;

public class SearchResultsPage extends BasePage {
<<<<<<< HEAD
=======

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#js-product-list");
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)

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
        waitForPageOpened();
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(productList);

        waitUntil(d ->
                        (productList != null && productList.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) ||
                                (!productCards.isEmpty()) ||
                                (!noMatchesHeader.isEmpty()) ||
                                (!pageNotFoundSection.isEmpty()),
                getDefaultWaitTimeout());
    }

    public boolean isDisplayed() {
<<<<<<< HEAD
=======
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
        return productList.isElementPresent(getDefaultWaitTimeout()) || isNoMatchesMessageDisplayed();
    }

    public boolean hasAnyProductTitleContaining(String keyword) {
        String k = keyword.toLowerCase(Locale.ROOT);

        return productTitles.stream()
                .map(ExtendedWebElement::getText)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .anyMatch(t -> t.contains(k));
    }

    public boolean isNoMatchesMessageDisplayed() {
<<<<<<< HEAD
        waitUntil(d -> !noMatchesHeader.isEmpty() || !pageNotFoundSection.isEmpty() || !productCards.isEmpty(), getDefaultWaitTimeout());
        return noMatchesHeader.stream().anyMatch(e -> e != null && e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
                || pageNotFoundSection.stream().anyMatch(e -> e != null && e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC));
    }

    public int getVisibleProductCardCount() {
        final int shortTimeout = TimeConstants.SHORT_TIMEOUT_SEC;

        waitUntil(d ->
                        productCards.stream().anyMatch(e -> e.isElementPresent(shortTimeout))
                                || noMatchesHeader.stream().anyMatch(e -> e.isElementPresent(shortTimeout))
                                || pageNotFoundSection.stream().anyMatch(e -> e.isElementPresent(shortTimeout)),
                getDefaultWaitTimeout()
        );
=======
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
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)

        if (isNoMatchesMessageDisplayed()) return 0;

        int retries = 2;
        for (int i = 0; i < retries; i++) {
            try {
<<<<<<< HEAD
                return (int) productCards.stream()
                        .filter(e -> e.isElementPresent(shortTimeout))
                        .count();
=======
                return (int) productCards.stream().filter(ExtendedWebElement::isDisplayed).count();
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
            } catch (StaleElementReferenceException e) {
                if (i == retries - 1) throw e;
            }
        }
        return 0;
    }

<<<<<<< HEAD
    public ProductPage openFirstVisibleProduct() {
        ExtendedWebElement first = findFirstVisibleProductTitle();
=======
    public ProductPage openFirstDisplayedProduct() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        ExtendedWebElement first = firstVisibleProductTitle();
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
        first.click();
        return new ProductPage(getDriver());
    }

    public String getFirstVisibleProductTitle() {
        return findFirstVisibleProductTitle().getText().trim();
    }

<<<<<<< HEAD
    public List<String> getVisibleProductTitles() {
        waitUntil(d -> productTitles != null && !productTitles.isEmpty(), getDefaultWaitTimeout());
        return productTitles.stream()
                .filter(e -> e != null && e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
=======
    public List<String> getDisplayedProductTitles() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        waitUntil(d -> productTitles != null && !productTitles.isEmpty(), getDefaultWaitTimeout());
        return productTitles.stream()
                .filter(ExtendedWebElement::isDisplayed)
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
                .map(ExtendedWebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

<<<<<<< HEAD
    private ExtendedWebElement findFirstVisibleProductTitle() {
        long timeoutSec = getDefaultWaitTimeout().getSeconds();

        waitUntil(d -> productTitles.stream().anyMatch(e -> isVisibleSafe(e, timeoutSec)), timeoutSec);

        return productTitles.stream()
                .filter(e -> isVisibleSafe(e, timeoutSec))
=======
    private ExtendedWebElement firstVisibleProductTitle() {
        waitUntil(d -> productTitles.stream().anyMatch(ExtendedWebElement::isDisplayed), getDefaultWaitTimeout());
        return productTitles.stream()
                .filter(ExtendedWebElement::isDisplayed)
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible product titles found"));
    }

    private boolean isVisibleSafe(ExtendedWebElement element, long timeoutSec) {
        if (element == null) return false;
        try {
            return element.isElementPresent(timeoutSec);
        } catch (StaleElementReferenceException ignored) {
            return false;
        }
    }
}
