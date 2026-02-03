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

    @FindBy(id = "onetrust-accept-btn-handler")
    private WebElement acceptCookiesBtn;

    @FindBy(css = ".change-country-close")
    private WebElement closeCountryPopupBtn;

    @FindBy(id = "header-big-screen-search-box")
    private WebElement searchBar;

    @FindBy(xpath = "//*[@data-testid='plp-results-title']")
    private WebElement resultsTitle;

    @FindBy(xpath = "//*[@data-testid='plp-no-results-text']")
    private WebElement noResultsText;

    @FindBy(xpath = "//*[@data-testid='product_summary_title']")
    private List<WebElement> productTitles;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void acceptCookiesIfPresent() {
        try {
            WebDriverWait wait = new WebDriverWait(driver,
                    Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
            wait.until(ExpectedConditions.elementToBeClickable(acceptCookiesBtn));
            acceptCookiesBtn.click();
        } catch (TimeoutException | NoSuchElementException ignored) {
        }
    }

    public void dismissCountryPopupIfPresent() {
        try {
            WebDriverWait wait = new WebDriverWait(driver,
                    Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
            wait.until(ExpectedConditions.elementToBeClickable(closeCountryPopupBtn));
            closeCountryPopupBtn.click();
        } catch (TimeoutException | NoSuchElementException ignored) {
        }
    }

    public void searchProduct(String productName) {
        acceptCookiesIfPresent();
        dismissCountryPopupIfPresent();
        WebDriverWait wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        wait.until(ExpectedConditions.visibilityOf(searchBar));
        wait.until(ExpectedConditions.elementToBeClickable(searchBar));
        searchBar.clear();
        searchBar.sendKeys(productName);
        searchBar.sendKeys(Keys.ENTER);
    }

    public boolean isSearchResultsPageLoaded() {
        try {
            return resultsTitle.isDisplayed();
        } catch (NoSuchElementException e) {
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

    public boolean hasProductContaining(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return productTitles.stream()
                .map(WebElement::getText)
                .anyMatch(title -> title.toLowerCase().contains(lowerKeyword));
    }

    public boolean isSearchTermInUrl(String keyword) {
        return driver.getCurrentUrl().toLowerCase().contains(keyword.toLowerCase());
    }
}
