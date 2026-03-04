package com.solvd.pages.desktop;

import com.solvd.pages.common.ProductPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = ProductPageBase.class)
public class ProductPage extends ProductPageBase {

    @FindBy(css = "#_desktop_cart .cart-products-count")
    private ExtendedWebElement desktopCartCount;

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected ExtendedWebElement getCartCountElement() {
        return desktopCartCount;
    }
}