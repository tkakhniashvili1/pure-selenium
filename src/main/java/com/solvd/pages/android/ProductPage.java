package com.solvd.pages.android;

import com.solvd.pages.common.ProductPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = ProductPageBase.class)
public class ProductPage extends ProductPageBase {

    @FindBy(css = "#_mobile_cart .cart-products-count")
    private ExtendedWebElement mobileCartCount;

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected ExtendedWebElement getCartCountElement() {
        return mobileCartCount;
    }
}