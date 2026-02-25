package com.solvd.pages.common;

import com.solvd.components.CartItem;
import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.math.BigDecimal;
import java.util.List;

import static com.solvd.utils.ParseUtils.parseIntegerFromText;
import static com.solvd.utils.ParseUtils.parseMoney;

public abstract class CartPageBase extends BasePage {

    private static final String ATTRIBUTE_TEXT_CONTENT = "textContent";

    @FindBy(id = "main")
    private ExtendedWebElement pageRoot;

    @FindBy(css = "#main .cart-items .cart-item")
    private List<CartItem> cartItems;

    @FindBy(css = "#cart-subtotal-products .value, .cart-summary-line.cart-subtotal .value, .cart-subtotal .value")
    private ExtendedWebElement subtotal;

    @FindBy(css = ".cart-total .value")
    private ExtendedWebElement total;

    @FindBy(css = "#main .no-items")
    private ExtendedWebElement emptyCartMessage;

    @FindBy(css = ".cart-products-count")
    private ExtendedWebElement cartCount;

    public CartPageBase(WebDriver driver) {
        super(driver);
        setUiLoadedMarker(pageRoot);
        waitForPageOpened();
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(pageRoot);
        pageRoot.isElementPresent(getDefaultWaitTimeout());

        waitUntil(d ->
                        cartItems.stream().anyMatch(ExtendedWebElement::isVisible)
                                || emptyCartMessage.isVisible(),
                getDefaultWaitTimeout());
    }

    public int getQuantity() {
        if (isEmptyCartMessageDisplayed()) return 0;

        CartItem item = firstCartItem();
        if (item == null) {
            throw new IllegalStateException("Cart item not found, but cart is not empty");
        }
        return item.quantity();
    }

    public BigDecimal getProductsSubtotal() {
        return readMoneyFrom(subtotal);
    }

    public BigDecimal getTotal() {
        return readMoneyFrom(total);
    }

    public void increaseQuantityTo(int targetQuantity) {
        CartItem item = firstCartItem();
        if (item == null) {
            throw new NoSuchElementException("Cart item not found");
        }

        int currentQuantity = item.quantity();

        int attempts = 0;
        int maxAttempts = Math.max(10, targetQuantity - currentQuantity + 3);

        while (currentQuantity < targetQuantity && attempts < maxAttempts) {
            int before = currentQuantity;

            item.increase();

            waitUntil(d -> {
                CartItem refreshed = firstCartItem();
                return refreshed != null && refreshed.quantity() > before;
            }, getDefaultWaitTimeout());

            CartItem refreshed = firstCartItem();
            if (refreshed == null) {
                throw new NoSuchElementException("Cart item not found");
            }
            currentQuantity = refreshed.quantity();

            attempts++;
        }

        if (currentQuantity < targetQuantity) {
            throw new IllegalStateException("Unable to increase quantity to " + targetQuantity);
        }
    }

    public int getCartLinesCount() {
        if (isEmptyCartMessageDisplayed()) return 0;
        return cartItems.size();
    }

    public void removeFirstLine() {
        if (isEmptyCartMessageDisplayed()) return;

        if (cartItems.isEmpty() || !cartItems.get(0).isDisplayed()) {
            throw new NoSuchElementException("Cart item not found");
        }

        int before = cartItems.size();

        cartItems.get(0).remove();

        waitUntil(d -> isEmptyCartMessageDisplayed() || cartItems.size() < before,
                getDefaultWaitTimeout());
    }

    public boolean isEmptyCartMessageDisplayed() {
        return emptyCartMessage.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC);
    }

    public int cartCountElement() {
        if (!cartCount.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) return 0;
        return parseIntegerFromText(cartCount.getText());
    }

    private BigDecimal readMoneyFrom(ExtendedWebElement element) {
        waitUntil(d -> isMoneyValuePresent(element), getDefaultWaitTimeout());
        return parseMoney(element.getAttribute(ATTRIBUTE_TEXT_CONTENT));
    }

    private boolean isMoneyValuePresent(ExtendedWebElement element) {
        if (!element.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) return false;
        String text = element.getAttribute(ATTRIBUTE_TEXT_CONTENT);
        if (text == null) return false;

        text = text.trim();
        return !text.isEmpty() && text.chars().anyMatch(Character::isDigit);
    }

    private CartItem firstCartItem() {
        if (cartItems.isEmpty()) return null;

        ExtendedWebElement root = cartItems.get(0);
        if (!root.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) return null;

        return new CartItem(getDriver(), root.getElement());
    }
}