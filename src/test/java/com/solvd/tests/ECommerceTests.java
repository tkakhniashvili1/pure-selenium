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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static com.solvd.utils.UiActions.normalizeText;

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

    @BeforeMethod
    public void openBaseUrl() {
        driver.manage().deleteAllCookies();
        driver.get(ConfigReader.getProperty("base.url"));
    }

    @Test
    public void verifySuccessfulProductSearch() {
        HomePage homePage = new HomePage(driver);
        String query = homePage.getSearchKeywordFromHome();

        SearchResultsPage resultsPage = homePage.search(query);

        Assert.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");

        int count = resultsPage.getDisplayedProductCardsCountAllowZero();

        Assert.assertTrue(count > 0, "Number of displayed product cards should be > 0");
        resultsPage.getDisplayedProductTitles().forEach(System.out::println);
        Assert.assertTrue(resultsPage.hasAnyProductTitleContaining(query), "At least one product title should contain '" + query);
    }

    @Test
    public void verifyProductSearchWithNoResults() {
        HomePage homePage = new HomePage(driver);

        String query = "wkjnefjnfinerifgnrenfgjnrbvbvbvbvbvbvbbvbvbvbvbbvbvbv";
        SearchResultsPage resultsPage = homePage.search(query);

        Assert.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");
        Assert.assertTrue(resultsPage.isNoMatchesMessageDisplayed(), "No matches message should be displayed.");
        Assert.assertEquals(resultsPage.getDisplayedProductCardsCountAllowZero(), 0,
                "Displayed product cards should be 0 for a no-results search.");
    }

    @Test
    public void verifyProductDetailsPageOpensFromSearchResults() {
        HomePage homePage = new HomePage(driver);
        String query = homePage.getSearchKeywordFromHome();

        SearchResultsPage resultsPage = homePage.search(query);
        Assert.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");
        Assert.assertTrue(resultsPage.getDisplayedProductCardsCountAllowZero() > 0,
                "Search should return at least 1 product.");

        String clickedTitle = resultsPage.getFirstDisplayedProductTitle();
        Assert.assertFalse(normalizeText(clickedTitle).isEmpty(), "Clicked product title is empty.");

        ProductPage productPage = resultsPage.openFirstDisplayedProduct();

        Assert.assertTrue(productPage.isAddToCartVisibleAndEnabled(),
                "Add to cart button is not visible/enabled.");

        String pdpTitle = productPage.getTitle();
        Assert.assertFalse(normalizeText(pdpTitle).isEmpty(), "PDP title is empty.");

        Assert.assertTrue(
                normalizeText(pdpTitle).contains(normalizeText(clickedTitle)) ||
                        normalizeText(clickedTitle).contains(normalizeText(pdpTitle)),
                "PDP title should match/contain clicked product title."
        );
    }

    @Test
    public void verifyAddToCartFromProductDetailsPage() {
        HomePage homePage = new HomePage(driver);
        ProductPage productPage = homePage.openFirstHomeProductPdp();

        String pdpTitle = productPage.getTitle();
        int before = productPage.getCartCount();

        productPage.selectRequiredOptionsIfPresent();
        productPage.addToCart();

        Assert.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");
        Assert.assertTrue(productPage.getModalItemsCount() > 0, "Modal cart items count should be > 0.");

        Assert.assertEquals(
                normalizeText(productPage.getModalProductName()),
                normalizeText(pdpTitle),
                "Modal shows incorrect product name (should match PDP title)."
        );

        int after = productPage.waitForCartCountToIncrease(before);
        Assert.assertTrue(after > before, "Cart count should increase after add to cart.");
    }

    @Test
    public void verifyCartQuantityUpdateRecalculatesTotals() {
        HomePage homePage = new HomePage(driver);

        ProductPage productPage = homePage.openFirstHomeProductPdp();
        productPage.selectRequiredOptionsIfPresent();
        productPage.addToCart();

        Assert.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");

        CartPage cartPage = productPage.openCartFromModal();
        Assert.assertTrue(cartPage.isDisplayed(), "Cart page not displayed (cart lines not visible).");

        BigDecimal subtotal1 = cartPage.getProductsSubtotal();
        BigDecimal total1 = cartPage.getTotal();

        int targetQty = 2;
        cartPage.increaseQuantityTo(targetQty);

        Assert.assertEquals(cartPage.getQuantity(), targetQty, "Quantity value was not updated.");

        BigDecimal subtotal2 = cartPage.getProductsSubtotal();
        BigDecimal total2 = cartPage.getTotal();

        Assert.assertTrue(subtotal2.compareTo(subtotal1) > 0, "Products subtotal should increase after qty increase.");
        Assert.assertTrue(total2.compareTo(total1) > 0, "Total should increase after quantity increase.");
    }

    @Test
    public void verifyRemovingProductEmptiesTheCart() {
        HomePage homePage = new HomePage(driver);
        ProductPage productPage = homePage.openFirstHomeProductPdp();

        productPage.selectRequiredOptionsIfPresent();
        productPage.addToCart();

        Assert.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");

        CartPage cartPage = productPage.openCartFromModal();
        Assert.assertTrue(cartPage.isDisplayed(), "Cart page not displayed.");

        Assert.assertTrue(cartPage.getCartLinesCount() > 0, "Cart should have at least 1 product line.");

        cartPage.removeFirstLine();

        Assert.assertEquals(cartPage.getCartLinesCount(), 0, "Product line should be removed from the cart.");
        Assert.assertTrue(cartPage.isEmptyCartMessageDisplayed(), "Empty cart message should be displayed.");
        Assert.assertEquals(cartPage.getHeaderCartCount(), 0, "Cart quantity indicator should be 0.");
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
