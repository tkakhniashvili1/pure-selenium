package com.solvd.pages.common;

import com.solvd.utils.TimeConstants;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.By;
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

    @FindBy(css = "#blockcart-modal .cart-content p.cart-products-count")
    private ExtendedWebElement modalCartItemsLine;

    @FindBy(css = ".product-variants input[type='radio']")
    private List<ExtendedWebElement> variantRadios;

    @FindBy(css = "#blockcart-modal")
    private ExtendedWebElement blockcartModal;

    public ProductPageBase(WebDriver driver) {
        super(driver);
        ensureFrontOfficeIframeOnce(productTitle);
        setUiLoadedMarker(productTitle);
    }

    public String getTitle() {
        return productTitle.getText().trim();
    }

    public boolean isAddToCartButtonPresent() {
        return addToCartButton.isElementPresent();
    }

    public void selectRequiredOptionsIfPresent() {
        selectFirstAvailableOptions(variantSelects, "option");
        selectOneRadioPerGroup(variantRadios);
    }

    public void addProductToCart() {
        addToCartButton.click();
    }

    public int getModalItemsCount() {
        return parseCount(modalCartItemsLine.getText());
    }

    public boolean isAddToCartModalDisplayed() {
        return blockcartModal.isElementPresent();
    }

    public String getModalProductName() {
        modalProductName.waitUntil(driver ->
                        modalProductName.isElementPresent(),
                TimeConstants.SHORT_TIMEOUT_SEC);

        return modalProductName.getText().trim();
    }

    public int getCartCount() {
        ExtendedWebElement el = getCartCountElement();
        if (el == null || !el.isElementPresent(TimeConstants.SHORT_TIMEOUT_SEC)) {
            return 0;
        }
        return parseCount(el.getText());
    }

    public int waitForCartCountToBeIncremented(int initialCount) {
        waitUntil(d -> getCartCount() > initialCount, 5);
        return getCartCount();
    }

    public CartPageBase openCartFromModal() {
        proceedToCheckoutButton.click();
        return initPage(getDriver(), CartPageBase.class);
    }

    protected abstract ExtendedWebElement getCartCountElement();

    private void selectFirstAvailableOptions(List<ExtendedWebElement> selects, String tagName) {
        for (ExtendedWebElement select : selects) {

            select.findExtendedWebElements(By.tagName(tagName))
                    .stream()
                    .filter(option -> {
                        String value = option.getAttribute("value");
                        return value != null &&
                                !value.isBlank() &&
                                !"0".equals(value) &&
                                !option.isSelected();
                    })
                    .findFirst()
                    .ifPresent(ExtendedWebElement::click);
        }
    }

    private void selectOneRadioPerGroup(List<ExtendedWebElement> radios) {
        Set<String> pickedNames = new HashSet<>();

        for (ExtendedWebElement radio : radios) {
            if (!radio.isElementPresent()) continue;

            String name = radio.getAttribute("name");
            if (name == null || name.isBlank() || !pickedNames.add(name)) continue;

            if (!radio.isSelected()) {
                radio.click();
            }
        }
    }
}