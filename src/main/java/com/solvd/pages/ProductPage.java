package com.solvd.pages;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.solvd.utils.ParseUtil.parseCount;

public class ProductPage extends BasePage {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#main h1");

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
    }

    public void waitForPageOpened() {
        ensureFrontOfficeIframeOnce(PAGE_READY_LOCATOR);
        productTitle.isElementPresent(getDefaultWaitTimeout());
    }

    public String getTitle() {
        waitForPageOpened();
        return productTitle.getText().trim();
    }

    public boolean isAddToCartVisibleAndEnabled() {
        waitForPageOpened();
        return addToCartButton.isDisplayed() && addToCartButton.isEnabled();
    }

    public void selectRequiredOptionsIfPresent() {
        waitForPageOpened();

        for (ExtendedWebElement selectVariant : variantSelects) {
            if (selectVariant == null || !selectVariant.isElementPresent(1)) continue;

            Select select = new Select(selectVariant.getElement());
            select.getOptions().stream()
                    .filter(WebElement::isEnabled)
                    .map(opt -> opt.getAttribute("value"))
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .ifPresent(select::selectByValue);
        }

        Set<String> pickedNames = new HashSet<>();
        for (ExtendedWebElement radioEl : variantRadios) {
            if (radioEl == null || !radioEl.isElementPresent(1)) continue;

            WebElement input = radioEl.getElement();
            String name = input.getAttribute("name");
            if (name == null || name.isBlank()) continue;
            if (!pickedNames.add(name)) continue;
            if (!input.isEnabled()) continue;

            if (!input.isSelected()) {
                try {
                    WebElement label = input.findElement(By.xpath("./ancestor::label[1]"));
                    label.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", input);
                }
            }
        }
    }

    public void addToCart() {
        waitForPageOpened();
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
        cartPage.waitForPageOpened();
        return cartPage;
    }

    private ExtendedWebElement getFirstAvailableCartCountElement() {
        if (desktopCartCount != null && !desktopCartCount.isEmpty()) return desktopCartCount.get(0);
        if (mobileCartCount != null && !mobileCartCount.isEmpty()) return mobileCartCount.get(0);
        return null;
    }
}
