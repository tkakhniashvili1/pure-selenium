package com.solvd.components;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractUIObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class CartItemComponent extends AbstractUIObject {

    private static final String ATTR_VALUE = "value";

    @FindBy(css = ".product-line-info a")
    private ExtendedWebElement productTitle;

    @FindBy(css = "button.js-increase-product-quantity")
    private ExtendedWebElement plusButton;

    @FindBy(css = "input.js-cart-line-product-quantity")
    private ExtendedWebElement quantityInput;

    @FindBy(css = "a.remove-from-cart")
    private ExtendedWebElement removeButton;

    public CartItemComponent(WebDriver driver, SearchContext searchContext) {
        super(driver, searchContext);
    }

    public boolean isDisplayed() {
        return removeButton.isElementPresent();
    }

    public void increaseQuantity() {
        if (!plusButton.isClickable()) {
            throw new NoSuchElementException("Quantity increase button not clickable");
        }
        plusButton.click();
    }

    public int getQuantity() {
        return Integer.parseInt(quantityInput.getAttribute("value"));
    }
}