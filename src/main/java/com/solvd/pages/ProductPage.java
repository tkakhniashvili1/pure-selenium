package com.solvd.pages;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.solvd.utils.ParseUtil.parseCount;

public class ProductPage extends BasePage {

    @FindBy(css = "#main h1")
    private ExtendedWebElement productTitle;

    @FindBy(css = "form#add-to-cart-or-refresh button[data-button-action='add-to-cart']")
    private ExtendedWebElement addToCartButton;

    @FindBy(css = ".product-variants select")
    private List<ExtendedWebElement> variantSelects;

    @FindBy(css = ".product-variants .color")
    private List<ExtendedWebElement> colorSwatches;

    @FindBy(css = "#blockcart-modal .product-name")
    private ExtendedWebElement modalProductName;

    @FindBy(css = "#blockcart-modal a.btn.btn-primary")
    private ExtendedWebElement proceedToCheckoutButton;

    @FindBy(css = "#_desktop_cart .cart-products-count")
    private List<ExtendedWebElement> desktopCartCount;

    @FindBy(css = "#_mobile_cart .cart-products-count")
    private List<ExtendedWebElement> mobileCartCount;

    @FindBy(css = "#blockcart-modal .cart-content p.cart-products-count")
    private ExtendedWebElement modalCartItemsLine;

    @FindBy(css = ".product-variants input[type='radio']")
    private List<ExtendedWebElement> variantRadios;

    @FindBy(css = "#blockcart-modal")
    private ExtendedWebElement blockcartModal;

    public ProductPage(WebDriver driver) {
        super(driver);
        waitForPageOpened();
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(productTitle);
        productTitle.isElementPresent(getDefaultWaitTimeout());
    }

    public String getTitle() {
        return productTitle.getText().trim();
    }

    public boolean isAddToCartVisibleAndEnabled() {
        return addToCartButton.isDisplayed() && addToCartButton.isEnabled();
    }

    public void selectRequiredOptionsIfPresent() {
        for (ExtendedWebElement selectVariant : variantSelects) {
            if (selectVariant == null || !selectVariant.isVisible()) continue;

            List<ExtendedWebElement> options = selectVariant.findExtendedWebElements(By.tagName("option"));
            for (ExtendedWebElement option : options) {
                if (option == null || !option.isVisible() || !option.isEnabled()) continue;

                String value = option.getAttribute("value");
                if (value != null && !value.isBlank()) {
                    option.click();
                    break;
                }
            }
        }

        Set<String> pickedNames = new HashSet<>();
        for (ExtendedWebElement variantRadio : variantRadios) {
            if (variantRadio == null || !variantRadio.isVisible() || !variantRadio.isEnabled()) continue;

            String name = variantRadio.getAttribute("name");
            if (name == null || name.isBlank()) continue;
            if (!pickedNames.add(name)) continue;

            if (!variantRadio.isSelected()) {
                variantRadio.click();
            }
        }
    }

    public void addToCart() {
        addToCartButton.click();
    }

    public int getModalItemsCount() {
        modalCartItemsLine.isElementPresent();
        return parseCount(modalCartItemsLine.getText());
    }

    public boolean isAddToCartModalDisplayed() {
        blockcartModal.isElementPresent();
        return blockcartModal.isDisplayed();
    }

    public String getModalProductName() {
        modalProductName.isElementPresent();
        return modalProductName.getText().trim();
    }

    public int getCartCount() {
        ExtendedWebElement el = getFirstAvailableCartCountElement();
        if (el == null) return 0;
        return parseCount(el.getText());
    }

    public int waitForCartCountToIncrease(int initialCount) {
        int expectedCount = initialCount + 1;

        waitUntil(d -> {
            try {
                return getCartCount() >= expectedCount;
            } catch (StaleElementReferenceException | NoSuchElementException ignored) {
                return false;
            }
        }, getDefaultWaitTimeout());

        return getCartCount();
    }

    public CartPage openCartFromModal() {
        proceedToCheckoutButton.click();

        getDriver().switchTo().defaultContent();
        CartPage cartPage = new CartPage(getDriver());

        return cartPage;
    }

    private ExtendedWebElement getFirstAvailableCartCountElement() {
        if (desktopCartCount != null && !desktopCartCount.isEmpty()) return desktopCartCount.get(0);
        if (mobileCartCount != null && !mobileCartCount.isEmpty()) return mobileCartCount.get(0);
        return null;
    }
}
