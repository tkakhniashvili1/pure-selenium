package com.solvd.components;

import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractUIObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class CartItem extends AbstractUIObject {

    private static final String ATTR_VALUE = "value";

    @FindBy(css = ".product-line-info a")
    private ExtendedWebElement productTitle;

    @FindBy(css = "button.js-increase-product-quantity")
    private ExtendedWebElement plusButton;

    @FindBy(css = "input.js-cart-line-product-quantity")
    private ExtendedWebElement quantityInput;

    @FindBy(css = "a.remove-from-cart")
    private ExtendedWebElement removeButton;

    public CartItem(WebDriver driver, SearchContext searchContext) {
        super(driver, searchContext);
    }

    public boolean isDisplayed() {
        return removeButton.isElementPresent();
    }

    public String title() {
        return productTitle.getText().trim();
    }

    public void increase() {
        if (!plusButton.isElementPresent() || !plusButton.isEnabled()) {
            throw new NoSuchElementException("Quantity increase button not found or disabled");
        }
        plusButton.click();
    }

    public void remove() {
        if (!removeButton.isElementPresent()) {
            throw new NoSuchElementException("Remove button not found");
        }
        removeButton.click();
    }

    public int quantity() {
        String value = quantityInput.getAttribute(ATTR_VALUE);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Quantity input value is empty");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Quantity is not a number: " + value, e);
        }
    }
}