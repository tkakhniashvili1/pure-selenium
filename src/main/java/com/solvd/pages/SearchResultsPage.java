package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.solvd.utils.UiActions.click;

public class SearchResultsPage {

    protected WebDriver driver;
    private final WebDriverWait wait;

    private static final By BANNER_CHIPS_BY = By.cssSelector("[data-testid='search-banner-image-chip']");
    private static final By PRODUCT_TITLES_BY = By.cssSelector("[data-testid='product_summary_title']");

    private static final Set<String> URL_FILTER_KEYS = Set.of(
            "colour", "brand", "size", "length", "style", "fit",
            "category", "gender", "neckline", "pattern", "material"
    );

    @FindBy(css = "[data-testid='plp-results-title']")
    private WebElement resultsTitle;

    @FindBy(css = "[data-testid='plp-no-results-text']")
    private WebElement noResultsText;

    @FindBy(css = "[data-testid='product_summary_title']")
    private List<WebElement> productTitleLabels;

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        PageFactory.initElements(driver, this);
    }

    public boolean isSearchResultsPageLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(resultsTitle));
            return resultsTitle.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public boolean hasResults() {
        try {
            return !noResultsText.isDisplayed();
        } catch (NoSuchElementException e) {
            return true;
        }
    }

    public boolean isProductWithTitle(String text) {
        String needle = text.toLowerCase();
        return productTitleLabels.stream()
                .map(WebElement::getText)
                .map(String::toLowerCase)
                .anyMatch(title -> title.contains(needle));
    }

    public Set<String> getAppliedFilters() {
        Set<String> applied = new HashSet<>();

        String currentUrl = driver.getCurrentUrl();
        String path = URI.create(currentUrl).getPath();

        for (String segment : path.split("/")) {
            if (segment.isBlank()) continue;

            String[] tokens = segment.split("-");
            for (int i = 0; i < tokens.length - 1; i++) {
                String key = tokens[i].toLowerCase(Locale.ROOT);
                if (URL_FILTER_KEYS.contains(key)) {
                    String value = tokens[i + 1];
                    applied.add(normalize(value));
                }
            }
        }
        return applied;
    }

    public boolean isFilterApplied(String expected) {
        return getAppliedFilters().stream().anyMatch(f -> f.equalsIgnoreCase(expected));
    }

    public boolean isNoResultsTextContainsCountAndQuery(String query) {
        try {
            wait.until(ExpectedConditions.visibilityOf(noResultsText));
            String text = noResultsText.getText().toLowerCase();
            return text.contains("0") && text.contains("results") && text.contains(query.toLowerCase());
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public ProductPage openFirstProductFromResults() {
        wait.until(d -> productTitleLabels != null && !productTitleLabels.isEmpty());
        click(driver, wait, productTitleLabels.get(0));
        return new ProductPage(driver);
    }

    public void applyColorFilter(String color) {
        String beforeUrl = driver.getCurrentUrl();
        WebElement beforeFirst = firstProductTitleEl();

        click(driver, wait, findChipByText(color));

        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(beforeUrl)));
        wait.until(ExpectedConditions.stalenessOf(beforeFirst));
        wait.until(d -> d.findElements(PRODUCT_TITLES_BY).size() > 0);
    }

    public boolean firstNProductTitlesContain(String keyword, int n) {
        int limit = Math.min(n, productTitleLabels.size());
        if (limit == 0) return false;

        String k = keyword.toLowerCase(Locale.ROOT);
        for (int i = 0; i < limit; i++) {
            String title = productTitleLabels.get(i).getText().toLowerCase(Locale.ROOT);
            if (!title.contains(k)) return false;
        }
        return true;
    }

    public boolean isFilterAppliedReliable(String expectedColor) {
        boolean urlOk = isFilterApplied(expectedColor);
        boolean resultsOk = firstNProductTitlesContain(expectedColor, 5);
        return urlOk && resultsOk;
    }

    public List<String> getProductTitles() {
        wait.until(d -> productTitleLabels != null && !productTitleLabels.isEmpty());

        return productTitleLabels.stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public void printProductTitles() {
        for (String t : getProductTitles()) {
            System.out.println(t);
        }
    }

    private WebElement firstProductTitleEl() {
        wait.until(d -> d.findElements(PRODUCT_TITLES_BY).size() > 0);
        return driver.findElements(PRODUCT_TITLES_BY).get(0);
    }

    private WebElement findChipByText(String text) {
        wait.until(d -> d.findElements(BANNER_CHIPS_BY).size() > 0);

        List<WebElement> chips = driver.findElements(BANNER_CHIPS_BY);
        for (WebElement chip : chips) {
            String t = chip.getText();
            if (t != null && t.trim().equalsIgnoreCase(text)) return chip;
        }
        throw new NoSuchElementException("Chip not found: " + text);
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) return s;
        String low = s.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(low.charAt(0)) + low.substring(1);
    }
}
