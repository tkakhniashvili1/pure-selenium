package com.solvd.pages.common;

import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.solvd.utils.ParseUtils.parseCount;

public abstract class ProductPageBase extends BasePage {

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
    private ExtendedWebElement desktopCartCount;

    @FindBy(css = "#_mobile_cart .cart-products-count")
    private ExtendedWebElement mobileCartCount;

    @FindBy(css = "#blockcart-modal .cart-content p.cart-products-count")
    private ExtendedWebElement modalCartItemsLine;

    @FindBy(css = ".product-variants input[type='radio']")
    private List<ExtendedWebElement> variantRadios;

    @FindBy(css = "#blockcart-modal")
    private ExtendedWebElement blockcartModal;

    public ProductPageBase(WebDriver driver) {
        super(driver);
        waitForPageOpened();
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(productTitle);
        productTitle.isElementPresent();
    }

    public String getTitle() {
        return productTitle.getText().trim();
    }

    public boolean isAddToCartButtonPresent() {
        return addToCartButton.isElementPresent(getDefaultWaitTimeout()) && addToCartButton.isEnabled();
    }

    public void selectRequiredOptionsIfPresent() {
        selectFirstAvailableOptions(variantSelects, "option");
        selectFirstAvailableRadios(variantRadios);
    }

    public void addProductToCart() {
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

    public int waitForCartCountToBeIncremented(int initialCount) {
        final int expectedCount = initialCount + 1;
        final ExtendedWebElement[] holder = {getFirstAvailableCartCountElement()};

        waitUntil(d -> {
            try {
                return holder[0] != null && parseCount(holder[0].getText()) >= expectedCount;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                holder[0] = getFirstAvailableCartCountElement();
                return false;
            }
        }, getDefaultWaitTimeout());

        return holder[0] == null ? 0 : parseCount(holder[0].getText());
    }

    public CartPageBase openCartFromModal() {
        proceedToCheckoutButton.click();
        return initPage(getDriver(), CartPageBase.class);
    }

    private ExtendedWebElement getFirstAvailableCartCountElement() {
        if (desktopCartCount.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) return desktopCartCount;
        if (mobileCartCount.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) return mobileCartCount;
        return null;
    }

    private void selectFirstAvailableOptions(List<ExtendedWebElement> selects, String tagName) {
        for (ExtendedWebElement select : selects) {
            if (!select.isElementPresent(getDefaultWaitTimeout())) continue;

            List<ExtendedWebElement> options = select.findExtendedWebElements(By.tagName(tagName));
            for (ExtendedWebElement option : options) {
                if (!option.isElementPresent(getDefaultWaitTimeout())) continue;

                String value = option.getAttribute("value");
                if (value != null && !value.isBlank()) {
                    option.click();
                    break;
                }
            }
        }
    }

    private void selectFirstAvailableRadios(List<ExtendedWebElement> radios) {
        Set<String> pickedNames = new HashSet<>();

        for (ExtendedWebElement radio : radios) {
            if (!radio.isElementPresent(getDefaultWaitTimeout())) continue;

            String name = radio.getAttribute("name");
            if (name == null || name.isBlank() || !pickedNames.add(name)) continue;

            if (!radio.isSelected()) {
                radio.click();
            }
        }
    }
}