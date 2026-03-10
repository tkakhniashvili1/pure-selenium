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

    @FindBy(id = "content")
    private ExtendedWebElement content;

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
        ensureFrontOfficeIframeOnce(content);
    }

    @Override
    public boolean isPageOpened() {
        ensureFrontOfficeIframeOnce(content);
        int timeoutSec = TimeConstants.SHORT_TIMEOUT_SEC;

        return (productList != null && productList.isElementPresent(timeoutSec))
                || noMatchesHeader.stream().anyMatch(e -> e.isElementPresent(timeoutSec))
                || pageNotFoundSection.stream().anyMatch(e -> e.isElementPresent(timeoutSec));
    }

    public boolean hasAnyProductTitleContaining(String keyword) {
        String k = keyword.toLowerCase(Locale.ROOT);

        return productTitles.stream()
                .map(ExtendedWebElement::getText)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .anyMatch(t -> t.contains(k));
    }

    public boolean isNoMatchesMessageDisplayed() {
        return noMatchesHeader.stream().anyMatch(e -> e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
                || pageNotFoundSection.stream().anyMatch(e -> e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC));
    }

    public int getVisibleProductCardCount() {
        final int shortTimeout = TimeConstants.SHORT_TIMEOUT_SEC;

        if (isNoMatchesMessageDisplayed()) {
            return 0;
        }

        try {
            waitUntil(driver -> productCards.stream()
                            .anyMatch(e -> e.isElementPresent(shortTimeout)),
                    shortTimeout);

            return (int) productCards.stream()
                    .filter(e -> e.isElementPresent(shortTimeout))
                    .count();

        } catch (StaleElementReferenceException e) {

            return 0;
        }
    }

    public ProductPageBase openFirstVisibleProduct() {
        ExtendedWebElement first = findFirstVisibleProductTitle();
        first.click();
        return initPage(getDriver(), ProductPageBase.class);
    }

    public String getFirstVisibleProductTitle() {
        return findFirstVisibleProductTitle().getText().trim();
    }

    public List<String> getVisibleProductTitles() {
        return productTitles.stream()
                .filter(e -> e != null && e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
                .map(ExtendedWebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private ExtendedWebElement findFirstVisibleProductTitle() {
        return productTitles.stream()
                .filter(e -> e != null && e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible product titles found"));
    }
}