package com.solvd.pages.ios;

import com.solvd.pages.common.HomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.IOS_PHONE, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
    public HomePage(WebDriver driver) {
        super(driver);
    }
}
