package com.solvd.pages;

import com.solvd.utils.ParseUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductPage extends AbstractPage {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#main h1");

    @FindBy(css = "#main h1")
    private WebElement productTitle;

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
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }

    public String getTitle() {
        ensureLoaded();
        return getText(productTitle, "productTitle");
    }

    public boolean isAddToCartVisibleAndEnabled() {
        ensureLoaded();
        wait.until(d -> addToCartButton.isDisplayed());
        return addToCartButton.isDisplayed() && addToCartButton.isEnabled();
    }

    public void selectRequiredOptionsIfPresent() {
        ensureLoaded();

        for (WebElement selectVariant : variantSelects) {
            Select select = new Select(selectVariant);
            select.getOptions().stream()
                    .filter(opt -> opt.isEnabled()
                            && opt.getAttribute("value") != null
                            && !opt.getAttribute("value").isBlank())
                    .findFirst()
                    .ifPresent(opt -> select.selectByValue(opt.getAttribute("value")));
        }

        Set<String> pickedNames = new HashSet<>();
        for (WebElement label : variantRadios) {
            WebElement input;
            try {
                input = label.findElement(By.cssSelector("input[type='radio']"));
            } catch (NoSuchElementException ignored) {
                continue;
            }

            String name = input.getAttribute("name");
            if (name != null && !name.isBlank() && input.isEnabled() && pickedNames.add(name)) {
                click(label, "variantRadioLabel");
            }
        }

        colorSwatches.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .ifPresent(swatch -> click(swatch, "colorSwatch"));
    }

    public void addToCart() {
        ensureLoaded();
        click(addToCartButton, "addToCartButton");
    }

    public int getModalItemsCount() {
        wait.until(d -> modalCartItemsLine.isDisplayed());
        return ParseUtil.parseCount(getText(modalCartItemsLine, "modalCartItemsLine"));
    }

    public boolean isAddToCartModalDisplayed() {
        wait.until(d -> blockcartModal.isDisplayed());
        return blockcartModal.isDisplayed();
    }

    public String getModalProductName() {
        wait.until(d -> modalProductName.isDisplayed());
        return modalProductName.getText().trim();
    }

    public int getCartCount() {
        WebElement el = getFirstAvailableCartCountElement();
        if (el == null) return 0;
        return ParseUtil.parseCount(getTextContent(el));
    }

    public int waitForCartCountToIncrease(int initialCount) {
        int expectedCount = initialCount + 1;

        wait.until(d -> {
            try {
                return getCartCount() >= expectedCount;
            } catch (StaleElementReferenceException | NoSuchElementException ignored) {
                return false;
            }
        });

        return getCartCount();
    }

    public CartPage openCartFromModal() {
        click(proceedToCheckoutButton, "proceedToCheckoutButton");

        driver.switchTo().defaultContent();

        CartPage cartPage = new CartPage(driver);
        cartPage.waitForPageOpened();
        return cartPage;
    }

    private WebElement getFirstAvailableCartCountElement() {
        if (desktopCartCount != null && !desktopCartCount.isEmpty()) return desktopCartCount.get(0);
        if (mobileCartCount != null && !mobileCartCount.isEmpty()) return mobileCartCount.get(0);
        return null;
    }
}
