package com.solvd.components;

import com.solvd.pages.CartPage;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractUIObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class CartItemModalComponent extends AbstractUIObject {

    @FindBy(css = ".modal-body .product-name")
    private ExtendedWebElement productName;

    @FindBy(css = ".cart-content p.cart-products-count")
    private ExtendedWebElement cartProductsCount;

    @FindBy(css = "a.btn.btn-primary")
    private ExtendedWebElement proceedToCheckoutButton;

    public CartItemModalComponent(WebDriver driver, SearchContext searchContext) {
        super(driver, searchContext);
    }

    public CartItemModalComponent waitUntilOpened(long timeoutSec) {
        if (!productName.isPresent(timeoutSec)) {
            throw new NoSuchElementException("Cart modal was not opened");
        }
        return this;
    }

    public boolean isOpened() {
        return productName.isPresent(3);
    }

    public String getModalProductName() {
        return productName.getText();
    }

    public int getItemsCount() {
        String digits = cartProductsCount.getText().replaceAll("\\D+", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    public CartPage proceedToCheckout() {
        proceedToCheckoutButton.click();
        return new CartPage(getDriver());
    }
}