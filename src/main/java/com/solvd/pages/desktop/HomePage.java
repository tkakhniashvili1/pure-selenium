package com.solvd.pages.desktop;

import com.solvd.pages.common.HomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#search_widget input[name='s']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }
}
