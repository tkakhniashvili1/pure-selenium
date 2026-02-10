package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

public class CartPage {

    protected final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "iframe#framelive")
    private List<WebElement> iframes;

    @FindBy(css = "#main .cart-items .cart-item")
    private List<WebElement> cartItems;

    @FindBy(css = "input.js-cart-line-product-quantity")
    private List<WebElement> cartItemQuantities;

    @FindBy(css = "button.js-increase-product-quantity")
    private List<WebElement> qtyPlusButtons;

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
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait")))
        );
        PageFactory.initElements(driver, this);
    }

    private void ensureFrontOfficeIframe() {
        if (findFirstVisibleElement(cartItems, emptyCartMessage) != null) return;

        driver.switchTo().defaultContent();
        wait.until(d -> iframes != null && !iframes.isEmpty());
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframes.get(0)));
    }

    public void waitForLoaded() {
        ensureFrontOfficeIframe();
        wait.until(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null);
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
        ensureFrontOfficeIframe();
        wait.until(d -> findFirstVisibleElement(cartItemQuantities) != null);

        String v = findFirstVisibleElement(cartItemQuantities).getAttribute("value");
        return (v == null || v.isBlank()) ? 0 : Integer.parseInt(v.trim());
    }

    public BigDecimal getProductsSubtotal() {
        ensureFrontOfficeIframe();
        wait.until(d -> findFirstVisibleElement(subtotal) != null);
        return parseMoney(findFirstVisibleElement(subtotal).getText());
    }

    public BigDecimal getTotal() {
        ensureFrontOfficeIframe();
        wait.until(d -> findFirstVisibleElement(total) != null);
        return parseMoney(findFirstVisibleElement(total).getText());
    }

    public void increaseQuantityTo(int target) {
        ensureFrontOfficeIframe();
        wait.until(d -> findFirstVisibleElement(qtyPlusButtons) != null && findFirstVisibleElement(qtyPlusButtons).isEnabled());

        while (getQuantity() < target) {
            int beforeQty = getQuantity();
            BigDecimal beforeSubtotal = getProductsSubtotal();

            findFirstVisibleElement(qtyPlusButtons).click();

            wait.until(d ->
                    getQuantity() > beforeQty &&
                            getProductsSubtotal().compareTo(beforeSubtotal) > 0
            );
        }

        wait.until(d -> getQuantity() == target);
    }

    public int getCartLinesCount() {
        ensureFrontOfficeIframe();
        if (findFirstVisibleElement(emptyCartMessage) != null) return 0;
        return (int) cartItems.stream().filter(el -> el != null && el.isDisplayed()).count();
    }

    public void removeFirstLine() {
        ensureFrontOfficeIframe();
        wait.until(d -> findFirstVisibleElement(cartItems, emptyCartMessage) != null);

        wait.until(d -> findFirstVisibleElement(removeButtons) != null && findFirstVisibleElement(removeButtons).isEnabled());
        findFirstVisibleElement(removeButtons).click();

        wait.until(d -> findFirstVisibleElement(cartItems) == null && findFirstVisibleElement(emptyCartMessage) != null);
    }

    public boolean isEmptyCartMessageDisplayed() {
        ensureFrontOfficeIframe();
        return findFirstVisibleElement(emptyCartMessage) != null;
    }

    public int getHeaderCartCount() {
        ensureFrontOfficeIframe();
        WebElement el = findFirstVisibleElement(cartCount);
        return (el == null) ? 0 : parseIntegerFromText(textContent(el));
    }

    private WebElement findFirstVisibleElement(List<WebElement>... groups) {
        for (List<WebElement> g : groups) {
            if (g == null) continue;
            for (WebElement el : g) {
                try {
                    if (el != null && el.isDisplayed()) return el;
                } catch (StaleElementReferenceException | NoSuchElementException ignored) {
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

    private String textContent(WebElement el) {
        Object v = ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", el);
        return v == null ? "" : v.toString().trim();
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
