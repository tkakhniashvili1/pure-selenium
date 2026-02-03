package com.solvd.tests;

import com.solvd.pages.HomePage;
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

        homePage.searchProduct("dress");

        Assert.assertTrue(homePage.isSearchResultsPageLoaded(), "Search results page is not loaded.");
        Assert.assertTrue(homePage.hasResults(), "No search results found.");
        Assert.assertTrue(homePage.isSearchTermInUrl("dress"), "URL does not contain 'dress'.");
        Assert.assertTrue(homePage.isProductWithTitle("dress"), "No product contains 'dress'.");
    }

    @Test
    public void verifyWomenDressesFilteredByColor() {
        driver.get(ConfigReader.getProperty("base.url"));
        HomePage homePage = new HomePage(driver);

        homePage.applyWomenDressesFilters();

        Assert.assertTrue(homePage.hasResults(), "No products are displayed after applying filters");
        Assert.assertTrue(homePage.areFirstProductsBlackDresses(), "Not all products are black dresses");
    }

    @Test
    public void verifyFirstProductFromResultsNavigatesToPdp() {
        driver.get(ConfigReader.getProperty("base.url"));
        HomePage homePage = new HomePage(driver);

        homePage.searchProduct("jeans");

        Assert.assertTrue(homePage.isSearchResultsPageLoaded(), "Search results page did not load");
        Assert.assertTrue(homePage.hasResults(), "Expected results, but got none");

        homePage.openFirstProductFromResults();

        Assert.assertTrue(homePage.isProductDetailsLoaded(), "Product details page did not load");
    }

    @Test
    public void verifyNoResultsMessageForInvalidSearch() {
        driver.get(ConfigReader.getProperty("base.url"));
        HomePage homePage = new HomePage(driver);

        String query = "zzzzzzzzzz";
        homePage.searchProduct(query);

        Assert.assertTrue(homePage.isSearchResultsPageLoaded(), "Search results page is not loaded.");
        Assert.assertFalse(homePage.hasResults(), "Expected no results, but results were found.");
        Assert.assertTrue(homePage.isNoResultsTextContainsCountAndQuery(query),
                "No results text does not contain expected count/query.");
    }

    @Test
    public void verifyAddToBagFromPdp() {
        driver.get(ConfigReader.getProperty("base.url"));
        HomePage homePage = new HomePage(driver);

        homePage.searchProduct("jeans");

        Assert.assertTrue(homePage.isSearchResultsPageLoaded(), "Search results page did not load");
        Assert.assertTrue(homePage.hasResults(), "Expected results, but got none");
        Assert.assertTrue(homePage.isSearchTermInUrl("jeans"), "URL does not contain 'jeans'");

        homePage.openFirstProductFromResults();

        Assert.assertTrue(homePage.isProductDetailsLoaded(), "Product details page did not load");

        homePage.addCurrentProductToBag();

        Assert.assertTrue(homePage.isViewBagButtonVisible(), "View Bag is not visible after adding to bag");
    }

    @Test
    public void verifyCheckoutRedirectsToSignInForGuest() {
        driver.get(ConfigReader.getProperty("base.url"));
        HomePage homePage = new HomePage(driver);

        homePage.searchProduct("boots");

        Assert.assertTrue(homePage.isSearchResultsPageLoaded(), "Search results page did not load");
        Assert.assertTrue(homePage.hasResults(), "Expected results, but got none");
        Assert.assertTrue(homePage.isSearchTermInUrl("boots"), "URL does not contain 'boots'");

        homePage.openFirstProductFromResults();

        Assert.assertTrue(homePage.isProductDetailsLoaded(), "Product details page did not load");

        homePage.addCurrentProductToBag();

        Assert.assertTrue(homePage.isViewBagButtonVisible(), "View Bag is not visible after adding to bag");

        homePage.proceedToCheckoutFromPdp();

        Assert.assertTrue(homePage.isSignInRegisterPageLoaded(), "Sign In / Register page did not open");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
