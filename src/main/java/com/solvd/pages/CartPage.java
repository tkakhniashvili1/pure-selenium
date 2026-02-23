package com.solvd.pages;

import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.math.BigDecimal;
import java.util.List;

import static com.solvd.utils.ParseUtil.parseIntegerFromText;
import static com.solvd.utils.ParseUtil.parseMoney;

public class CartPage extends BasePage {

    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_TEXT_CONTENT = "textContent";

    @FindBy(id = "main")
    private ExtendedWebElement pageRoot;

    @FindBy(css = "#main .cart-items .cart-item")
    private List<ExtendedWebElement> cartItems;

    @FindBy(css = "input.js-cart-line-product-quantity")
    private List<ExtendedWebElement> cartItemQuantities;

    @FindBy(css = "button.js-increase-product-quantity")
    private List<ExtendedWebElement> quantityPlusButtons;

    @FindBy(css = "#cart-subtotal-products .value, .cart-summary-line.cart-subtotal .value, .cart-subtotal .value")
    private List<ExtendedWebElement> subtotal;

    @FindBy(css = ".cart-total .value")
    private List<ExtendedWebElement> total;

    @FindBy(css = "a.remove-from-cart")
    private List<ExtendedWebElement> removeButtons;

    @FindBy(css = "#main .no-items")
    private List<ExtendedWebElement> emptyCartMessage;

    @FindBy(css = ".cart-products-count")
    private List<ExtendedWebElement> cartCount;

    public CartPage(WebDriver driver) {
        super(driver);
        waitForPageOpened();
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(pageRoot);
        waitUntil(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null,
                getDefaultWaitTimeout());
    }

    public boolean isDisplayed() {
        try {
            waitForPageOpened();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public int getQuantity() {
        ExtendedWebElement quantityInput = findFirstVisibleElement(cartItemQuantities);
        if (quantityInput == null) {
            return 0;
        }

        String value = quantityInput.getAttribute(ATTRIBUTE_VALUE);
        if (value == null || value.isBlank()) {
            return 0;
        }

        return Integer.parseInt(value.trim());
    }

    public BigDecimal getProductsSubtotal() {
        return readMoneyFrom(subtotal);
    }

    public BigDecimal getTotal() {
        return readMoneyFrom(total);
    }

    public void increaseQuantityTo(int targetQuantity) {
        while (getQuantity() < targetQuantity) {
            ExtendedWebElement plusButton = findFirstVisibleElement(quantityPlusButtons);
            if (plusButton == null || !plusButton.isEnabled()) {
                throw new NoSuchElementException("Quantity increase button not found or disabled");
            }

            int currentQuantity = getQuantity();
            plusButton.click();

            waitUntil(d -> getQuantity() > currentQuantity, getDefaultWaitTimeout());
        }
    }

    public int getCartLinesCount() {
        if (findFirstVisibleElement(emptyCartMessage) != null) return 0;
        return (int) cartItems.stream().filter(el -> el != null && el.isDisplayed()).count();
    }

    public void removeFirstLine() {
        if (isEmptyCartMessageDisplayed()) {
            return;
        }

        int initialCartLinesCount = getCartLinesCount();

        ExtendedWebElement firstRemoveButton = findFirstVisibleElement(removeButtons);
        if (firstRemoveButton == null) {
            throw new NoSuchElementException("Remove button not found");
        }

        firstRemoveButton.click();

        waitUntil(d ->
                        isEmptyCartMessageDisplayed() ||
                                getCartLinesCount() < initialCartLinesCount,
                getDefaultWaitTimeout());
    }

    public boolean isEmptyCartMessageDisplayed() {
        return findFirstVisibleElement(emptyCartMessage) != null;
    }

    public int cartCountElement() {
        ExtendedWebElement el = findFirstVisibleElement(cartCount);
        if (el == null) return 0;

        return parseIntegerFromText(el.getText());
    }

    private final ExtendedWebElement findFirstVisibleElement(List<ExtendedWebElement>... groups) {
        final int timeoutSec = TimeConstants.SHORT_TIMEOUT_SEC;

        for (List<ExtendedWebElement> group : groups) {
            if (group == null) continue;

            for (ExtendedWebElement el : group) {
                if (el != null && el.isElementPresent(timeoutSec)) {
                    return el;
                }
            }
        }
        return null;
    }

    private BigDecimal readMoneyFrom(List<ExtendedWebElement> elements) {
        waitUntil(d -> isMoneyValuePresent(elements), getDefaultWaitTimeout());

        ExtendedWebElement el = findFirstVisibleElement(elements);
        if (el == null) return BigDecimal.ZERO;

        return parseMoney(el.getAttribute(ATTRIBUTE_TEXT_CONTENT));
    }

    private boolean isMoneyValuePresent(List<ExtendedWebElement> elements) {
        ExtendedWebElement el = findFirstVisibleElement(elements);
        String t = (el == null) ? null : el.getAttribute(ATTRIBUTE_TEXT_CONTENT);
        return t != null && t.matches(".*\\d.*");
    }
}
