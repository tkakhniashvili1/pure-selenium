package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class HomePage {

    protected WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "onetrust-accept-btn-handler")
    private WebElement acceptCookiesButton;

    @FindBy(css = ".change-country-close")
    private WebElement closeCountryPopupButton;

    @FindBy(id = "header-big-screen-search-box")
    private WebElement searchBar;

    @FindBy(xpath = "//*[@data-testid='plp-results-title']")
    private WebElement resultsTitle;

    @FindBy(xpath = "//*[@data-testid='plp-no-results-text']")
    private WebElement noResultsText;

    @FindBy(xpath = "//*[@data-testid='product_summary_title']")
    private List<WebElement> productTitleLabels;

    @FindBy(xpath = "//*[@data-testid='meganav-primarynav-link-women']")
    private WebElement womenCategoryFilter;

    @FindBy(xpath = "//*[contains(@class, 'header-w2hs3w') and normalize-space(text())='All Dresses']")
    private WebElement allDressesSubcategoryFilter;

    @FindBy(xpath = "//*[@data-testid='search-banner-image-chip' and .//*[normalize-space(text())='Black']]")
    private WebElement dressBlackFilter;

    @FindBy(css = "h1")
    private WebElement pdpTitle;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        PageFactory.initElements(driver, this);
    }

    private void clickIfVisible(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (TimeoutException | NoSuchElementException ignored) {
        }
    }

    public void acceptCookiesIfPresent() {
        clickIfVisible(acceptCookiesButton);
    }

    public void dismissCountryPopupIfPresent() {
        clickIfVisible(closeCountryPopupButton);
    }

    public void searchProduct(String productName) {
        acceptCookiesIfPresent();
        dismissCountryPopupIfPresent();

        String beforeUrl = driver.getCurrentUrl();

        wait.until(ExpectedConditions.elementToBeClickable(searchBar));
        searchBar.click();
        searchBar.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchBar.sendKeys(Keys.BACK_SPACE);

        searchBar.sendKeys(productName);
        searchBar.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(beforeUrl)));
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
        return productTitleLabels.stream()
                .map(WebElement::getText)
                .map(String::toLowerCase)
                .anyMatch(title -> title.contains(text.toLowerCase()));
    }

    public boolean isSearchTermInUrl(String keyword) {
        return driver.getCurrentUrl().toLowerCase().contains(keyword.toLowerCase());
    }

    public void applyWomenDressesFilters() {
        acceptCookiesIfPresent();
        dismissCountryPopupIfPresent();

        wait.until(ExpectedConditions.elementToBeClickable(womenCategoryFilter)).click();
        wait.until(ExpectedConditions.elementToBeClickable(allDressesSubcategoryFilter)).click();
        wait.until(ExpectedConditions.elementToBeClickable(dressBlackFilter)).click();
    }

    public boolean areFirstProductsBlackDresses() {
        String[] dressKeywords = {"dress", "kaftan", "skirt", "gown", "tunic"};
        String[] blackKeywords = {"black", "jet", "charcoal"};

        for (int i = 0; i < 10; i++) {
            WebElement product = productTitleLabels.get(i);

            String fullTitle = (product.getAttribute("data-label") + " " +
                    product.getAttribute("data-desc") + " " +
                    product.getAttribute("title")).toLowerCase();

            boolean isDress = false;
            for (String keyword : dressKeywords) {
                if (fullTitle.contains(keyword)) {
                    isDress = true;
                    break;
                }
            }
            if (!isDress) return false;

            boolean isBlack = false;
            for (String keyword : blackKeywords) {
                if (fullTitle.contains(keyword)) {
                    isBlack = true;
                    break;
                }
            }
            if (!isBlack) return false;
        }

        return true;
    }

    public void openFirstProductFromResults() {
        acceptCookiesIfPresent();
        dismissCountryPopupIfPresent();

        wait.until(d -> productTitleLabels != null && productTitleLabels.size() > 0);
        wait.until(ExpectedConditions.elementToBeClickable(productTitleLabels.get(0))).click();
    }

    public boolean isProductDetailsLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pdpTitle));
            return pdpTitle.isDisplayed() && !pdpTitle.getText().trim().isEmpty();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
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

    @FindBy(xpath = "//*[@data-testid='item-form-addToBag-button']")
    private WebElement addToBagButton;

    @FindBy(xpath = "//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'view bag')]")
    private WebElement viewBagButton;

    private void safeClick(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element
        );
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void selectRandomSizeFromDropdownIfPresent() {
        try {
            WebElement chooseSize = driver.findElement(By.xpath(
                    "//*[(@role='combobox' or @aria-haspopup='listbox') and " +
                            "contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'choose size')]"
            ));
            safeClick(chooseSize);

            wait.until(d -> !driver.findElements(By.cssSelector("li[role='option'][data-value]")).isEmpty());
            List<WebElement> options = driver.findElements(By.cssSelector("li[role='option'][data-value]"));

            safeClick(options.get((int) (Math.random() * options.size())));
        } catch (Exception ignored) {
        }
    }

    public void addCurrentProductToBag() {
        acceptCookiesIfPresent();
        dismissCountryPopupIfPresent();

        selectRandomSizeFromDropdownIfPresent();
        safeClick(addToBagButton);
    }

    public boolean isViewBagButtonVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOf(viewBagButton));
            return viewBagButton.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
