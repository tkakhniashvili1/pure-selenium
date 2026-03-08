package com.solvd.pages.common;

import com.solvd.components.CartItemComponent;
import com.solvd.utils.ParseUtils;
import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.math.BigDecimal;
import java.util.List;

import static com.solvd.utils.ParseUtils.parseIntegerFromText;

public abstract class CartPageBase extends BasePage {

    private static final String ATTRIBUTE_TEXT_CONTENT = "textContent";

    @FindBy(id = "main")
    private ExtendedWebElement pageRoot;

    @FindBy(css = "#main .cart-items .cart-item")
    private List<CartItemComponent> cartItems;

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
    }

    @Override
    public boolean isPageOpened() {
        ensureFrontOfficeIframeOnce(pageRoot);
        return pageRoot.isElementPresent();
    }

    public int getQuantity() {
        if (isEmptyCartMessageDisplayed()) return 0;

        CartItemComponent item = getFirstCartItem();
        if (item == null) {
            throw new IllegalStateException("Cart item not found, but cart is not empty");
        }
        return item.getQuantity();
    }

    public BigDecimal getProductsSubtotal() {
        return parseMoney(subtotal);
    }

    public BigDecimal getTotal() {
        return parseMoney(total);
    }

    public void increaseQuantityTo(int targetQuantity) {
        CartItemComponent item = getFirstCartItem();
        if (item == null) {
            throw new NoSuchElementException("Cart item not found");
        }

        int currentQuantity = item.getQuantity();

        int attempts = 0;
        int maxAttempts = Math.max(10, targetQuantity - currentQuantity + 3);

        while (currentQuantity < targetQuantity && attempts < maxAttempts) {
            int before = currentQuantity;

            item.increaseQuantity();

            waitUntil(d -> {
                CartItemComponent refreshed = getFirstCartItem();
                return refreshed != null && refreshed.getQuantity() > before;
            }, getDefaultWaitTimeout());

            CartItemComponent refreshed = getFirstCartItem();
            if (refreshed == null) {
                throw new NoSuchElementException("Cart item not found");
            }
            currentQuantity = refreshed.getQuantity();

            attempts++;
        }

        if (currentQuantity < targetQuantity) {
            throw new IllegalStateException("Unable to increase quantity to " + targetQuantity);
        }
    }

    public int getCartLinesCount() {
        if (isEmptyCartMessageDisplayed()) return 0;
        return (int) cartItems.stream().filter(CartItemComponent::isDisplayed).count();
    }

    public void removeFirstLine() {
        if (isEmptyCartMessageDisplayed()) return;

        if (cartItems.isEmpty() || !cartItems.get(0).isDisplayed()) {
            throw new NoSuchElementException("Cart item not found");
        }

        int before = cartItems.size();

        cartItems.get(0).click();

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

    private boolean isMoneyValuePresent(ExtendedWebElement element) {
        if (!element.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) return false;
        String text = element.getAttribute(ATTRIBUTE_TEXT_CONTENT);
        if (text == null) return false;

        text = text.trim();
        return !text.isEmpty() && text.chars().anyMatch(Character::isDigit);
    }

    private CartItemComponent getFirstCartItem() {
        return cartItems.stream()
                .filter(e -> e.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC))
                .findFirst()
                .orElse(null);
    }

    private BigDecimal parseMoney(ExtendedWebElement element) {
        waitUntil(d -> isMoneyValuePresent(element), getDefaultWaitTimeout());
        return ParseUtils.parseMoney(
                element.getAttribute(ATTRIBUTE_TEXT_CONTENT)
        );
    }
}