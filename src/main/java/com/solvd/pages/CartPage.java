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

    @FindBy(css = ".cart-items .cart-item, .cart-item")
    private List<WebElement> cartLines;

    @FindBy(css = "input.js-cart-line-product-quantity")
    private List<WebElement> qtyInputs;

    @FindBy(css = "button.js-increase-product-quantity")
    private List<WebElement> qtyPlusButtons;

    @FindBy(css = "#cart-subtotal-products .value, .cart-summary-line.cart-subtotal .value, .cart-subtotal .value")
    private List<WebElement> subtotal;

    @FindBy(css = "#cart-total .value, .cart-summary-line.cart-total .value, .cart-summary-totals .cart-total .value, .cart-total .value")
    private List<WebElement> total;

    @FindBy(css = "a[data-link-action='delete-from-cart'], a.remove-from-cart")
    private List<WebElement> removeButtons;

    @FindBy(css = ".no-items")
    private List<WebElement> emptyCartMessage;

    @FindBy(css = "#_desktop_cart .cart-products-count, #_mobile_cart .cart-products-count")
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
        if (firstVisible(cartLines, emptyCartMessage) != null) return;

        driver.switchTo().defaultContent();
        wait.until(d -> iframes != null && !iframes.isEmpty());
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframes.get(0)));
    }

    public void waitForLoaded() {
        ensureFrontOfficeIframe();
        wait.until(d -> firstVisible(cartLines, emptyCartMessage) != null);
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
        wait.until(d -> firstVisible(qtyInputs) != null);

        String v = firstVisible(qtyInputs).getAttribute("value");
        return (v == null || v.isBlank()) ? 0 : Integer.parseInt(v.trim());
    }

    public BigDecimal getProductsSubtotal() {
        ensureFrontOfficeIframe();
        wait.until(d -> firstVisible(subtotal) != null);
        return parseMoney(firstVisible(subtotal).getText());
    }

    public BigDecimal getTotal() {
        ensureFrontOfficeIframe();
        wait.until(d -> firstVisible(total) != null);
        return parseMoney(firstVisible(total).getText());
    }

    public void increaseQuantityTo(int target) {
        ensureFrontOfficeIframe();
        wait.until(d -> firstVisible(qtyPlusButtons) != null && firstVisible(qtyPlusButtons).isEnabled());

        while (getQuantity() < target) {
            int beforeQty = getQuantity();
            BigDecimal beforeSubtotal = getProductsSubtotal();

            firstVisible(qtyPlusButtons).click();

            wait.until(d ->
                    getQuantity() > beforeQty &&
                            getProductsSubtotal().compareTo(beforeSubtotal) > 0
            );
        }

        wait.until(d -> getQuantity() == target);
    }

    public int getCartLinesCount() {
        ensureFrontOfficeIframe();
        if (firstVisible(emptyCartMessage) != null) return 0;
        return (int) cartLines.stream().filter(el -> el != null && el.isDisplayed()).count();
    }

    public void removeFirstLine() {
        ensureFrontOfficeIframe();
        wait.until(d -> firstVisible(cartLines, emptyCartMessage) != null);

        wait.until(d -> firstVisible(removeButtons) != null && firstVisible(removeButtons).isEnabled());
        firstVisible(removeButtons).click();

        wait.until(d -> firstVisible(cartLines) == null && firstVisible(emptyCartMessage) != null);
    }

    public boolean isEmptyCartMessageDisplayed() {
        ensureFrontOfficeIframe();
        return firstVisible(emptyCartMessage) != null;
    }

    public int getHeaderCartCount() {
        ensureFrontOfficeIframe();
        WebElement el = firstVisible(cartCount);
        return (el == null) ? 0 : parseCount(textContent(el));
    }

    private WebElement firstVisible(List<WebElement>... groups) {
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

    private int parseCount(String raw) {
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
