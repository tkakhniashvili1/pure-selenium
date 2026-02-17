package com.solvd.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.math.BigDecimal;
import java.util.List;

public class CartPage extends AbstractPage {

    private static final By PAGE_READY_LOCATOR = By.id("main");

    @FindBy(css = "#main .cart-items .cart-item")
    private List<WebElement> cartItems;

    @FindBy(css = "input.js-cart-line-product-quantity")
    private List<WebElement> cartItemQuantities;

    @FindBy(css = "button.js-increase-product-quantity")
    private List<WebElement> quantityPlusButtons;

    @FindBy(css = "#cart-subtotal-products .value, .cart-summary-line.cart-subtotal .value, .cart-subtotal .value")
    private List<WebElement> subtotal;

    @FindBy(css = ".cart-total .value")
    private List<WebElement> total;

    @FindBy(css = "a.remove-from-cart")
    private List<WebElement> removeButtons;

    @FindBy(css = "#main .no-items")
    private List<WebElement> emptyCartMessage;

    @FindBy(css = ".cart-products-count")
    private List<WebElement> cartCount;

    public CartPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }

    public void waitForPageOpened() {
        ensureLoaded();
        wait.until(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null);
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
        ensureLoaded();
        wait.until(d -> findFirstVisibleElement(cartItemQuantities) != null);

        WebElement quantity = findFirstVisibleElement(cartItemQuantities);
        String v = (quantity == null) ? null : getAttribute(quantity, "cartQtyInput", "value");
        return (v == null || v.isBlank()) ? 0 : Integer.parseInt(v.trim());
    }

    public BigDecimal getProductsSubtotal() {
        ensureLoaded();
        wait.until(d -> findFirstVisibleElement(subtotal) != null);

        WebElement el = findFirstVisibleElement(subtotal);
        String raw = (el == null) ? null : getText(el, "productsSubtotal");
        return parseMoney(raw);
    }

    public BigDecimal getTotal() {
        ensureLoaded();
        wait.until(d -> findFirstVisibleElement(total) != null);

        WebElement el = findFirstVisibleElement(total);
        String raw = (el == null) ? null : getText(el, "total");
        return parseMoney(raw);
    }

    public void increaseQuantityTo(int target) {
        ensureLoaded();
        wait.until(d -> {
            WebElement button = findFirstVisibleElement(quantityPlusButtons);
            return button != null && button.isEnabled();
        });

        while (getQuantity() < target) {
            int beforeQty = getQuantity();
            BigDecimal beforeSubtotal = getProductsSubtotal();

            WebElement button = findFirstVisibleElement(quantityPlusButtons);
            click(button, "quantityPlusButton");

            wait.until(d ->
                    getQuantity() > beforeQty &&
                            getProductsSubtotal().compareTo(beforeSubtotal) > 0
            );
        }

        wait.until(d -> getQuantity() == target);
    }

    public int getCartLinesCount() {
        ensureLoaded();
        if (findFirstVisibleElement(emptyCartMessage) != null) return 0;
        return (int) cartItems.stream().filter(el -> el != null && el.isDisplayed()).count();
    }

    public void removeFirstLine() {
        ensureLoaded();
        wait.until(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null);

        wait.until(d -> {
            WebElement button = findFirstVisibleElement(removeButtons);
            return button != null && button.isEnabled();
        });

        WebElement button = findFirstVisibleElement(removeButtons);
        click(button, "removeButton");

        wait.until(d -> findFirstVisibleElement(cartItems) == null && findFirstVisibleElement(emptyCartMessage) != null);
    }

    public boolean isEmptyCartMessageDisplayed() {
        ensureLoaded();
        return findFirstVisibleElement(emptyCartMessage) != null;
    }

    public int getHeaderCartCount() {
        ensureLoaded();
        WebElement el = findFirstVisibleElement(cartCount);
        return (el == null) ? 0 : parseIntegerFromText(getTextContent(el));
    }

    private WebElement findFirstVisibleElement(List<WebElement>... groups) {
        for (List<WebElement> g : groups) {
            if (g == null) continue;
            for (WebElement el : g) {
                try {
                    if (el != null && el.isDisplayed()) return el;
                } catch (StaleElementReferenceException | org.openqa.selenium.NoSuchElementException ignored) {
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
