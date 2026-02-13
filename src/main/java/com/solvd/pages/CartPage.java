package com.solvd.pages;

<<<<<<< HEAD
import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
=======
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.*;
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
import org.openqa.selenium.support.FindBy;

import java.math.BigDecimal;
import java.util.List;

<<<<<<< HEAD
import static com.solvd.utils.ParseUtil.parseIntegerFromText;
import static com.solvd.utils.ParseUtil.parseMoney;
=======
public class CartPage extends BasePage {
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)

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

<<<<<<< HEAD
    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(pageRoot);
        waitUntil(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null,
                getDefaultWaitTimeout());
=======
    public void waitForLoaded() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        waitUntil(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null, getDefaultWaitTimeout());
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
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
<<<<<<< HEAD
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
=======
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
       waitUntil(d -> findFirstVisibleElement(cartItemQuantities) != null, getDefaultWaitTimeout());

        ExtendedWebElement quantity = findFirstVisibleElement(cartItemQuantities);
        String v = (quantity == null) ? null : quantity.getAttribute("value");
        return (v == null || v.isBlank()) ? 0 : Integer.parseInt(v.trim());
    }

    public BigDecimal getProductsSubtotal() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);

        ExtendedWebElement el = findFirstVisibleElement(subtotal);
        if (el == null || !el.isElementPresent(getDefaultWaitTimeout())) return BigDecimal.ZERO;

        return parseMoney(el.getText());
    }

    public BigDecimal getTotal() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);

        waitUntil(d -> {
            ExtendedWebElement el = findFirstVisibleElement(total);
            return el != null && el.isElementPresent(1) && el.isDisplayed();
        }, getDefaultWaitTimeout());

        ExtendedWebElement el = findFirstVisibleElement(total);
        if (el == null) return BigDecimal.ZERO;

        return parseMoney(el.getText());
    }

    public void increaseQuantityTo(int target) {
        waitForLoaded();

        waitUntil(d -> findFirstVisibleElement(quantityPlusButtons) != null, getDefaultWaitTimeout());
        waitUntil(d -> findFirstVisibleElement(cartItemQuantities) != null, getDefaultWaitTimeout());

        while (true) {
            int current = getQuantity();
            if (current >= target) return;

            ExtendedWebElement plus = findFirstVisibleElement(quantityPlusButtons);
            if (plus == null || !plus.isEnabled()) throw new NoSuchElementException("Quantity + button not found/enabled");

            plus.click();

            int before = current;
            waitUntil(d -> getQuantity() > before, getDefaultWaitTimeout());
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
        }
    }

    public int getCartLinesCount() {
<<<<<<< HEAD
=======
        waitForLoaded();

>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
        if (findFirstVisibleElement(emptyCartMessage) != null) return 0;
        return (int) cartItems.stream().filter(el -> el != null && el.isDisplayed()).count();
    }

    public void removeFirstLine() {
<<<<<<< HEAD
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
=======
        waitForLoaded();

        if (findFirstVisibleElement(emptyCartMessage) != null) return;

        int before = getCartLinesCount();

        waitUntil(d -> findFirstVisibleElement(removeButtons) != null, getDefaultWaitTimeout());
        ExtendedWebElement remove = findFirstVisibleElement(removeButtons);
        if (remove == null) throw new NoSuchElementException("Remove button not found");

        remove.click();

        waitUntil(d -> {
            if (findFirstVisibleElement(emptyCartMessage) != null) return true;
            return getCartLinesCount() < before;
        }, getDefaultWaitTimeout());
    }

    public boolean isEmptyCartMessageDisplayed() {
        waitForLoaded();
        return findFirstVisibleElement(emptyCartMessage) != null;
    }

    public int getHeaderCartCount() {
        waitForLoaded();

>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
        ExtendedWebElement el = findFirstVisibleElement(cartCount);
        if (el == null) return 0;

        return parseIntegerFromText(el.getText());
    }

<<<<<<< HEAD
    private final ExtendedWebElement findFirstVisibleElement(List<ExtendedWebElement>... groups) {
        final int timeoutSec = TimeConstants.SHORT_TIMEOUT_SEC;

        for (List<ExtendedWebElement> group : groups) {
            if (group == null) continue;

            for (ExtendedWebElement el : group) {
                if (el != null && el.isElementPresent(timeoutSec)) {
                    return el;
=======
    private ExtendedWebElement findFirstVisibleElement(List<ExtendedWebElement>... groups) {
        for (List<ExtendedWebElement> g : groups) {
            if (g == null) continue;
            for (ExtendedWebElement el : g) {
                try {
                    if (el != null && el.isElementPresent(1) && el.isDisplayed()) return el;
                } catch (Exception ignored) {
>>>>>>> a8a2aee (Move code from pure selenium to Carina FW)
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
