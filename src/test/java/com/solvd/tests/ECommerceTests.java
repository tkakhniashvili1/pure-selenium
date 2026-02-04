package com.solvd.tests;

import com.solvd.pages.*;
import com.solvd.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class ECommerceTests {

    private WebDriver driver;

    @BeforeClass
    public void setup() {
        String browser = ConfigReader.getProperty("browser");
        switch (browser.toLowerCase()) {
            case "chrome":
                driver = new ChromeDriver();
                break;
            case "firefox":
                driver = new FirefoxDriver();
                break;
            case "edge":
                driver = new EdgeDriver();
                break;
            default:
                throw new RuntimeException("Unsupported browser: " + browser);
        }

        driver.manage().window().maximize();

        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("page.load.timeout")))
        );
    }

    @Test
    public void verifyProductSearch() {
        driver.get(ConfigReader.getProperty("base.url"));

        HomePage homePage = new HomePage(driver);

        SearchResultsPage resultsPage = homePage.searchProductByName("dress");

        Assert.assertTrue(resultsPage.isSearchResultsPageLoaded(), "Search results page is not loaded.");
        Assert.assertTrue(resultsPage.hasResults(), "No search results found.");
        Assert.assertTrue(homePage.isSearchTermInUrl("dress"), "URL does not contain 'dress'.");
        Assert.assertTrue(resultsPage.isProductWithTitle("dress"), "No product contains 'dress'.");
    }

    @Test
    public void verifyWomenDressesFilteredByColor() {
        driver.get(ConfigReader.getProperty("base.url"));

        HomePage homePage = new HomePage(driver);

        SearchResultsPage resultsPage = homePage.openCategorySubcategory("women", "All Dresses");

        resultsPage.applyColorFilter("Black");

        Assert.assertTrue(resultsPage.hasResults(), "No products are displayed after applying filters");
        Assert.assertTrue(resultsPage.isFilterApplied("Black"), "Black filter is not applied");
    }


    @Test
    public void verifySearchResultsTitlesPrinted() {
        driver.get(ConfigReader.getProperty("base.url"));

        HomePage homePage = new HomePage(driver);

        SearchResultsPage resultsPage = homePage.searchProductByName("dress");

        Assert.assertTrue(resultsPage.isSearchResultsPageLoaded(), "Search results page did not load");
        Assert.assertTrue(resultsPage.hasResults(), "Expected results, but got none");

        List<String> titles = resultsPage.getProductTitles();
        resultsPage.printProductTitles();

        Assert.assertTrue(titles.size() > 0, "Number of results displayed should be > 0");
        Assert.assertTrue(titles.stream().allMatch(t -> t != null && !t.trim().isEmpty()), "Some titles are empty");
    }

    @Test
    public void verifyNoResultsMessageForInvalidSearch() {
        driver.get(ConfigReader.getProperty("base.url"));

        HomePage homePage = new HomePage(driver);
        String query = "zzzzzzzzzz";

        SearchResultsPage resultsPage = homePage.searchProductByName(query);

        Assert.assertTrue(resultsPage.isSearchResultsPageLoaded(), "Search results page is not loaded.");
        Assert.assertFalse(resultsPage.hasResults(), "Expected no results, but results were found.");
        Assert.assertTrue(resultsPage.isNoResultsTextContainsCountAndQuery(query),
                "No results text does not contain expected count/query.");
    }

    @Test
    public void verifyAddToBagFromPdp() {
        driver.get(ConfigReader.getProperty("base.url"));

        HomePage homePage = new HomePage(driver);

        SearchResultsPage resultsPage = homePage.searchProductByName("jeans");

        Assert.assertTrue(resultsPage.isSearchResultsPageLoaded(), "Search results page did not load");
        Assert.assertTrue(resultsPage.hasResults(), "Expected results, but got none");

        ProductPage productPage = resultsPage.openFirstProductFromResults();

        Assert.assertTrue(productPage.isProductDetailsLoaded(), "Product details page did not load");

        productPage.addCurrentProductToBag();

        Assert.assertTrue(productPage.isViewBagButtonVisible(), "View Bag is not visible after adding to bag");
    }

    @Test
    public void verifyCheckoutRedirectsToSignInForGuest() {
        driver.get(ConfigReader.getProperty("base.url"));

        HomePage homePage = new HomePage(driver);

        SearchResultsPage resultsPage = homePage.searchProductByName("boots");
        Assert.assertTrue(resultsPage.isSearchResultsPageLoaded(), "Search results page did not load");
        Assert.assertTrue(resultsPage.hasResults(), "Expected results, but got none");

        ProductPage productPage = resultsPage.openFirstProductFromResults();

        Assert.assertTrue(productPage.isLoaded(), "Product details page did not load");

        productPage.addCurrentProductToBag();

        Assert.assertTrue(productPage.isViewBagButtonVisible(), "View Bag is not visible after adding to bag");

        BagPage bagPage = productPage.openBag();
        CheckoutPage checkoutPage = bagPage.proceedToCheckout();

        Assert.assertTrue(checkoutPage.isSignInRegisterPageLoaded(), "Sign In / Register page did not open");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
