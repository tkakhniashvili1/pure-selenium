package com.solvd.pages.desktop;

import com.solvd.pages.common.ProductPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = ProductPageBase.class)
public class ProductPage extends ProductPageBase {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#main h1");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }
}
