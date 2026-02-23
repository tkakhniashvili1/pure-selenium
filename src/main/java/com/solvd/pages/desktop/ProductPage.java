package com.solvd.pages.desktop;

import com.solvd.pages.common.ProductPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = ProductPageBase.class)
public class ProductPage extends ProductPageBase {
    public ProductPage(WebDriver driver) {
        super(driver);
    }
}