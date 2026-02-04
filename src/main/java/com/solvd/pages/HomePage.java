package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import com.solvd.utils.UiActions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.solvd.utils.UiActions.click;

public class HomePage {

    protected WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "onetrust-accept-btn-handler")
    private WebElement acceptCookiesButton;

    @FindBy(css = ".change-country-close")
    private WebElement closeCountryPopupButton;

    @FindBy(id = "header-big-screen-search-box")
    private WebElement searchBar;

    @FindBy(css = "[data-testid^='meganav-primarynav-link-']")
    private List<WebElement> categoryLinks;

    @FindBy(css = "ul[data-testid='catalogue-items']")
    private WebElement catalogueItemsList;

    @FindBy(css = "ul[data-testid='catalogue-items'] a[data-testid='catalogueItem-href']")
    private List<WebElement> catalogueItemLinks;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        PageFactory.initElements(driver, this);
    }

    public SearchResultsPage searchProductByName(String productName) {
        handlePopups();

        String beforeUrl = driver.getCurrentUrl();

        wait.until(ExpectedConditions.elementToBeClickable(searchBar));
        searchBar.click();
        searchBar.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        searchBar.sendKeys(Keys.BACK_SPACE);

        searchBar.sendKeys(productName);
        searchBar.sendKeys(Keys.ENTER);

        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(beforeUrl)));

        return new SearchResultsPage(driver);
    }

    public boolean isSearchTermInUrl(String keyword) {
        return driver.getCurrentUrl().toLowerCase().contains(keyword.toLowerCase());
    }

    public SearchResultsPage openCategorySubcategory(String category, String subcategory) {
        handlePopups();

        WebElement cat = findCategory(category);
        new Actions(driver).moveToElement(cat).perform();

        wait.until(ExpectedConditions.visibilityOf(catalogueItemsList));

        click(driver, wait, findCatalogueItemByTitle(subcategory));

        return new SearchResultsPage(driver);
    }

    private WebElement findCatalogueItemByTitle(String title) {
        for (WebElement a : catalogueItemLinks) {
            String t = a.getAttribute("title");
            if (t != null && t.trim().equalsIgnoreCase(title)) return a;
        }
        throw new NoSuchElementException("Subcategory not found: " + title);
    }

    private WebElement findCategory(String category) {
        String expected = ("meganav-primarynav-link-" + category).toLowerCase();
        for (WebElement el : categoryLinks) {
            String v = el.getAttribute("data-testid");
            if (v != null && v.toLowerCase().equals(expected)) return el;
        }
        throw new NoSuchElementException("Category not found: " + category);
    }

    private void handlePopups() {
        clickIfPresent(acceptCookiesButton);
        clickIfPresent(closeCountryPopupButton);
    }

    private void clickIfPresent(WebElement element) {
        UiActions.clickIfPresent(driver, wait, element);
    }
}
