package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

import static com.solvd.utils.UiActions.click;

public class SearchResultsPage {

    protected final WebDriver driver;
    private final WebDriverWait wait;

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
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait")))
        );
        PageFactory.initElements(driver, this);
    }

    public boolean isDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(productList));
            return true;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return isNoMatchesMessageDisplayed();
        }
    }

    public boolean hasAnyProductTitleContaining(String keyword) {
        String k = keyword.toLowerCase(Locale.ROOT);

        return productTitles.stream()
                .map(WebElement::getText)
                .map(t -> t.toLowerCase(Locale.ROOT))
                .anyMatch(t -> t.contains(k));
    }

    public boolean isNoMatchesMessageDisplayed() {
        wait.until(d -> !noMatchesHeader.isEmpty() || !pageNotFoundSection.isEmpty() || !productCards.isEmpty());
        return noMatchesHeader.stream().anyMatch(WebElement::isDisplayed)
                || pageNotFoundSection.stream().anyMatch(WebElement::isDisplayed);
    }

    public int getVisibleProductCardCount() {
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
        WebElement first = firstVisibleProductTitle();
        click(driver, wait, first);
        return new ProductPage(driver);
    }

    public String getFirstDisplayedProductTitle() {
        return firstVisibleProductTitle().getText().trim();
    }

    public List<String> getDisplayedProductTitles() {
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
