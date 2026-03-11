package com.solvd.pages.ios;

import com.solvd.pages.common.CartPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.IOS_PHONE, parentClass = CartPageBase.class)
public class CartPage extends CartPageBase {
    public CartPage(WebDriver driver) {
        super(driver);
    }
}
