package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.solvd.utils.UiActions.click;

public class ProductPage {

    protected final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "#main h1")
    private WebElement pdpTitle;

    @FindBy(css = "form#add-to-cart-or-refresh button[data-button-action='add-to-cart']")
    private WebElement addToCartButton;

    @FindBy(css = ".product-variants select")
    private List<WebElement> variantSelects;

    @FindBy(css = ".product-variants .color")
    private List<WebElement> colorSwatches;

    @FindBy(css = "#blockcart-modal .product-name")
    private WebElement modalProductName;

    @FindBy(css = "#blockcart-modal a.btn.btn-primary")
    private WebElement proceedToCheckoutButton;

    @FindBy(css = "#_desktop_cart .cart-products-count")
    private List<WebElement> desktopCartCount;

    @FindBy(css = "#_mobile_cart .cart-products-count")
    private List<WebElement> mobileCartCount;

    @FindBy(css = "#blockcart-modal .cart-content p.cart-products-count")
    private WebElement modalCartItemsLine;

    @FindBy(css = ".product-variants input[type='radio']")
    private List<WebElement> variantRadios;

    @FindBy(css = "#blockcart-modal")
    private WebElement blockcartModal;

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait")))
        );
        PageFactory.initElements(driver, this);
    }

    public String getTitle() {
        wait.until(ExpectedConditions.visibilityOf(pdpTitle));
        return pdpTitle.getText().trim();
    }

    public boolean isAddToCartVisibleAndEnabled() {
        wait.until(ExpectedConditions.visibilityOf(addToCartButton));
        return addToCartButton.isDisplayed() && addToCartButton.isEnabled();
    }

    public void selectRequiredOptionsIfPresent() {
        for (WebElement el : variantSelects) {
            Select s = new Select(el);
            for (WebElement opt : s.getOptions()) {
                String value = opt.getAttribute("value");
                if (value == null || value.isBlank() || opt.getAttribute("disabled") != null) continue;
                s.selectByValue(value);
                break;
            }
        }

        Set<String> picked = new HashSet<>();
        for (WebElement r : variantRadios) {
            String name = r.getAttribute("name");
            if (name == null || name.isBlank() || r.getAttribute("disabled") != null) continue;
            if (!picked.add(name)) continue;
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", r);
        }

        if (colorSwatches != null && !colorSwatches.isEmpty()) {
            WebElement swatch = colorSwatches.stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
            if (swatch != null) click(driver, wait, swatch);
        }
    }

    public void addToCart() {
        click(driver, wait, addToCartButton);
    }

    public int getModalItemsCount() {
        wait.until(ExpectedConditions.visibilityOf(modalCartItemsLine));
        return parseCount(modalCartItemsLine.getText());
    }

    public boolean isAddToCartModalDisplayed() {
        wait.until(ExpectedConditions.visibilityOf(blockcartModal));
        return blockcartModal.isDisplayed();
    }

    public String getModalProductName() {
        wait.until(ExpectedConditions.visibilityOf(modalProductName));
        return modalProductName.getText().trim();
    }

    public int getCartCount() {
        WebElement el = getCartCountElementOrNull();
        if (el == null) return 0;
        return parseCount(textContent(el));
    }

    public int waitForCartCountToIncrease(int before) {
        wait.until(ExpectedConditions.visibilityOf(modalCartItemsLine));
        int target = parseCount(modalCartItemsLine.getText());
        if (target <= before) target = before + 1;

        int finalTarget = target;

        wait.until(d -> {
            try { return getCartCount() >= finalTarget; }
            catch (StaleElementReferenceException ignored) { return false; }
        });

        return getCartCount();
    }

    private int parseCount(String raw) {
        if (raw == null) return 0;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        return Integer.parseInt(digits);
    }

    private WebElement getCartCountElementOrNull() {
        if (desktopCartCount != null && !desktopCartCount.isEmpty()) return desktopCartCount.get(0);
        if (mobileCartCount != null && !mobileCartCount.isEmpty()) return mobileCartCount.get(0);
        return null;
    }

    private String textContent(WebElement el) {
        Object v = ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;", el);
        return v == null ? "" : v.toString().trim();
    }

    public CartPage openCartFromModal() {
        wait.until(ExpectedConditions.elementToBeClickable(proceedToCheckoutButton));
        proceedToCheckoutButton.click();

        driver.switchTo().defaultContent();

        CartPage cartPage = new CartPage(driver);
        cartPage.waitForLoaded();
        return cartPage;
    }
}
