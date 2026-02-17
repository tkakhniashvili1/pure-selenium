package com.solvd.tests;

import com.solvd.pages.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static com.solvd.utils.UiActions.normalizeText;

public class ECommerceTests extends AbstractTest {

    @Test
    public void verifySuccessfulProductSearch() {
        HomePage homePage = new HomePage(getDriver());
        String query = homePage.getSearchKeywordFromHome();

        SearchResultsPage resultsPage = homePage.search(query);

        Assert.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");

        int count = resultsPage.getVisibleProductCardCount();

        Assert.assertTrue(count > 0, "Number of displayed product cards should be > 0");
        resultsPage.getDisplayedProductTitles().forEach(System.out::println);
        Assert.assertTrue(resultsPage.hasAnyProductTitleContaining(query), "At least one product title should contain '" + query);
    }

    @Test
    public void verifyProductSearchWithNoResults() {
        HomePage homePage = new HomePage(getDriver());

        String query = "wkjnefjnfinerifgnrenfgjnrbvbvbvbvbvbvbbvbvbvbvbbvbvbv";
        SearchResultsPage resultsPage = homePage.search(query);

        Assert.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");
        Assert.assertTrue(resultsPage.isNoMatchesMessageDisplayed(), "No matches message should be displayed.");
        Assert.assertEquals(resultsPage.getVisibleProductCardCount(), 0,
                "Displayed product cards should be 0 for a no-results search.");
    }

    @Test
    public void verifyProductDetailsPageOpensFromSearchResults() {
        HomePage homePage = new HomePage(getDriver());
        String query = homePage.getSearchKeywordFromHome();

        SearchResultsPage resultsPage = homePage.search(query);
        Assert.assertTrue(resultsPage.isDisplayed(), "Results page not displayed.");
        Assert.assertTrue(resultsPage.getVisibleProductCardCount() > 0,
                "Search should return at least 1 product.");

        String clickedTitle = resultsPage.getFirstVisibleProductTitle();
        Assert.assertFalse(normalizeText(clickedTitle).isEmpty(), "Clicked product title is empty.");

        ProductPage productPage = resultsPage.openFirstVisibleProduct();

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
        HomePage homePage = new HomePage(getDriver());
        ProductPage productPage = homePage.openFirstProduct();

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
        HomePage homePage = new HomePage(getDriver());

        ProductPage productPage = homePage.openFirstProduct();
        productPage.selectRequiredOptionsIfPresent();
        productPage.addToCart();

        Assert.assertTrue(productPage.isAddToCartModalDisplayed(), "Add-to-cart modal not displayed.");

        CartPage cartPage = productPage.openCartFromModal();
        Assert.assertTrue(cartPage.isDisplayed(), "Cart page not displayed (cart lines not visible).");

        BigDecimal subtotal1 = cartPage.getProductsSubtotal();
        BigDecimal total1 = cartPage.getTotal();

        int targetQuantity = 2;
        cartPage.increaseQuantityTo(targetQuantity);

        Assert.assertEquals(cartPage.getQuantity(), targetQuantity, "Quantity value was not updated.");

        BigDecimal subtotal2 = cartPage.getProductsSubtotal();
        BigDecimal total2 = cartPage.getTotal();

        Assert.assertTrue(subtotal2.compareTo(subtotal1) > 0, "Products subtotal should increase after quantity increase.");
        Assert.assertTrue(total2.compareTo(total1) > 0, "Total should increase after quantity increase.");
    }

    @Test
    public void verifyRemovingProductEmptiesTheCart() {
        HomePage homePage = new HomePage(getDriver());
        ProductPage productPage = homePage.openFirstProduct();

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
}