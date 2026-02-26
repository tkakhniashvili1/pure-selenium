package com.solvd.tests;

import com.solvd.pages.common.*;
import com.zebrunner.carina.core.AbstractTest;
import com.zebrunner.carina.utils.R;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.math.BigDecimal;

import static com.solvd.utils.TextUtils.normalizeText;

public class ECommerceTests extends AbstractTest {

    private SoftAssert softly;

    @Parameters({"capabilities.browserName"})
    @BeforeMethod(alwaysRun = true)
    public void setBrowserName(@Optional("") String browserName) {
        if (browserName != null && !browserName.isBlank()) {
            R.CONFIG.put("capabilities.browserName", browserName, true);
        }
    }

    @BeforeMethod
    public void initSoftAssert() {
        softly = new SoftAssert();
    }

    @Test
    public void verifySuccessfulProductSearch() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        String query = homePage.getSearchKeywordFromHome();

        SearchResultsPageBase resultsPage = homePage.search(query);

        softly.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");

        int count = resultsPage.getVisibleProductCardCount();

        softly.assertTrue(count > 0, "Number of displayed product cards should be > 0");
        resultsPage.getVisibleProductTitles().forEach(System.out::println);
        softly.assertTrue(resultsPage.hasAnyProductTitleContaining(query), "At least one product title should contain '" + query);

        softly.assertAll();
    }

    @Test
    public void verifyProductSearchWithNoResults() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);

        String query = "wkjnefjnfinerifgnrenfgjnrbvbvbvbvbvbvbbvbvbvbvbbvbvbv";
        SearchResultsPageBase resultsPage = homePage.search(query);

        softly.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");
        softly.assertTrue(resultsPage.isNoMatchesMessageDisplayed(), "No matches message should be displayed.");
        softly.assertEquals(resultsPage.getVisibleProductCardCount(), 0,
                "Displayed product cards should be 0 for a no-results search.");

        softly.assertAll();
    }

    @Test
    public void verifyProductDetailsPageOpensFromSearchResults() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        String query = homePage.getSearchKeywordFromHome();

        SearchResultsPageBase resultsPage = homePage.search(query);
        softly.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");
        softly.assertTrue(resultsPage.getVisibleProductCardCount() > 0,
                "Search should return at least 1 product.");

        String clickedTitle = resultsPage.getFirstVisibleProductTitle();
        softly.assertFalse(normalizeText(clickedTitle).isEmpty(), "Clicked product title is empty.");

        ProductPageBase productPage = resultsPage.openFirstVisibleProduct();

        softly.assertTrue(productPage.isAddToCartButtonPresent(),
                "Add to cart button is not visible/enabled.");

        String pdpTitle = productPage.getTitle();
        softly.assertFalse(normalizeText(pdpTitle).isEmpty(), "PDP title is empty.");

        softly.assertTrue(
                normalizeText(pdpTitle).contains(normalizeText(clickedTitle)) ||
                        normalizeText(clickedTitle).contains(normalizeText(pdpTitle)),
                "PDP title should match/contain clicked product title."
        );

        softly.assertAll();
    }

    @Test
    public void verifyAddToCartFromProductDetailsPage() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        ProductPageBase productPage = homePage.openFirstProduct();

        String pdpTitle = productPage.getTitle();
        int before = productPage.getCartCount();

        productPage.selectRequiredOptionsIfPresent();
        productPage.addProductToCart();

        softly.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");
        softly.assertTrue(productPage.getModalItemsCount() > 0, "Modal cart items count should be > 0.");

        softly.assertEquals(
                normalizeText(productPage.getModalProductName()),
                normalizeText(pdpTitle),
                "Modal shows incorrect product name (should match PDP title)."
        );

        int after = productPage.waitForCartCountToBeIncremented(before);
        softly.assertTrue(after > before, "Cart count should increase after add to cart.");

        softly.assertAll();
    }

    @Test
    public void verifyCartQuantityUpdateRecalculatesTotals() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);

        ProductPageBase productPage = homePage.openFirstProduct();
        productPage.selectRequiredOptionsIfPresent();
        productPage.addProductToCart();

        softly.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");

        CartPageBase cartPage = productPage.openCartFromModal();
        softly.assertTrue(cartPage.isPageOpened(), "Cart page not displayed (cart lines not visible).");

        BigDecimal subtotal1 = cartPage.getProductsSubtotal();
        BigDecimal total1 = cartPage.getTotal();

        int targetQuantity = 2;
        cartPage.increaseQuantityTo(targetQuantity);

        softly.assertEquals(cartPage.getQuantity(), targetQuantity, "Quantity value was not updated.");

        BigDecimal subtotal2 = cartPage.getProductsSubtotal();
        BigDecimal total2 = cartPage.getTotal();

        softly.assertTrue(subtotal2.compareTo(subtotal1) > 0, "Products subtotal should change after quantity increase.");
        softly.assertTrue(total2.compareTo(total1) > 0, "Total should increase after quantity increase.");

        softly.assertAll();
    }

    @Test
    public void verifyCartIsEmptyAfterRemovingLastProduct() {
        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        ProductPageBase productPage = homePage.openFirstProduct();

        productPage.selectRequiredOptionsIfPresent();
        productPage.addProductToCart();

        softly.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");

        CartPageBase cartPage = productPage.openCartFromModal();

        softly.assertTrue(cartPage.isPageOpened(), "Cart page not displayed.");
        softly.assertTrue(cartPage.getCartLinesCount() > 0, "Cart should have at least 1 product line.");

        cartPage.removeFirstLine();

        softly.assertEquals(cartPage.getCartLinesCount(), 0, "Product line should be removed from the cart.");
        softly.assertTrue(cartPage.isEmptyCartMessageDisplayed(), "Empty cart message should be displayed.");
        softly.assertEquals(cartPage.cartCountElement(), 0, "Cart quantity indicator should be 0.");

        softly.assertAll();
    }
}