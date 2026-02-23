package com.solvd.pages.android;

import com.solvd.pages.common.CartPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = CartPageBase.class)
public class CartPage extends CartPageBase {
    public CartPage(WebDriver driver) {
        super(driver);
    }
}