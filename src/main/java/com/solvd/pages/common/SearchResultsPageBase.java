package com.solvd.pages.common;

import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Locale;

public abstract class SearchResultsPageBase extends BasePage {

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

    public SearchResultsPageBase(WebDriver driver) {
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

        if (isNoMatchesMessageDisplayed()) return 0;

        int retries = 2;
        for (int i = 0; i < retries; i++) {
            try {
                return (int) productCards.stream()
                        .filter(e -> e.isElementPresent(shortTimeout))
                        .count();
            } catch (StaleElementReferenceException e) {
                if (i == retries - 1) throw e;
            }
        }
        return 0;
    }

    public ProductPageBase openFirstVisibleProduct() {
        ExtendedWebElement first = findFirstVisibleProductTitle();
        first.click();
        return PageFactory.initPage(getDriver(), ProductPageBase.class);
    }

    public String getFirstVisibleProductTitle() {
        return findFirstVisibleProductTitle().getText().trim();
    }

    public List<String> getVisibleProductTitles() {
        waitUntil(d -> productTitles != null && !productTitles.isEmpty(), getDefaultWaitTimeout());
        return productTitles.stream()
                .filter(e -> e != null && e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
                .map(ExtendedWebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private ExtendedWebElement findFirstVisibleProductTitle() {
        long timeoutSec = getDefaultWaitTimeout().getSeconds();

        waitUntil(d -> productTitles.stream().anyMatch(e -> isVisibleSafe(e, timeoutSec)), timeoutSec);

        return productTitles.stream()
                .filter(e -> isVisibleSafe(e, timeoutSec))
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