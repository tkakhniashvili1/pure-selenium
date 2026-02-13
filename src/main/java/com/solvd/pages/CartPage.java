package com.solvd.pages;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;

import java.math.BigDecimal;
import java.util.List;

public class CartPage extends BasePage {

    private static final By PAGE_READY_LOCATOR = By.id("main");

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
    }

    public void waitForLoaded() {
        ensureFrontOfficeIframe(PAGE_READY_LOCATOR);
        waitUntil(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null, getDefaultWaitTimeout());
    }

    public boolean isDisplayed() {
        try {
            waitForLoaded();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public int getQuantity() {
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
        }
    }

    public int getCartLinesCount() {
        waitForLoaded();

        if (findFirstVisibleElement(emptyCartMessage) != null) return 0;
        return (int) cartItems.stream().filter(el -> el != null && el.isDisplayed()).count();
    }

    public void removeFirstLine() {
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

        ExtendedWebElement el = findFirstVisibleElement(cartCount);
        if (el == null) return 0;

        return parseIntegerFromText(el.getText());
    }

    private ExtendedWebElement findFirstVisibleElement(List<ExtendedWebElement>... groups) {
        for (List<ExtendedWebElement> g : groups) {
            if (g == null) continue;
            for (ExtendedWebElement el : g) {
                try {
                    if (el != null && el.isElementPresent(1) && el.isDisplayed()) return el;
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private int parseIntegerFromText(String raw) {
        if (raw == null) return 0;
        String digits = raw.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private BigDecimal parseMoney(String raw) {
        if (raw == null) return BigDecimal.ZERO;

        String s = raw.replaceAll("[^0-9,\\.]", "");

        long commas = s.chars().filter(ch -> ch == ',').count();
        if (commas == 1 && s.indexOf('.') == -1) s = s.replace(',', '.');
        else s = s.replace(",", "");

        return s.isBlank() ? BigDecimal.ZERO : new BigDecimal(s);
    }
}
