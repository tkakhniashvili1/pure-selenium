package com.solvd.pages.desktop;

import com.solvd.pages.common.HomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void handleNativePopup() {
        throw new UnsupportedOperationException("Native context switching is not supported for desktop.");
    }

    @Override
    public void switchBackToWeb() {
        throw new UnsupportedOperationException("Native context switching is not supported for desktop.");
    }
}