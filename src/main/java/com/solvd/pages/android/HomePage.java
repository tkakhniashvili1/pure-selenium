package com.solvd.pages.android;

import com.solvd.pages.common.HomePageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;

import java.util.Set;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {
    public HomePage(WebDriver driver) {
        super(driver);
    }

    public Set<String> getAvailableContexts() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        return driver.getContextHandles();
    }

    public void handleNativePopup() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());

        driver.context("NATIVE_APP");
        waitUntil(d -> "NATIVE_APP".equals(driver.getContext()), getDefaultWaitTimeout());

        driver.navigate().back();
    }

    public void switchBackToWeb() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        for (String context : driver.getContextHandles()) {
            String uppercaseContext = context.toUpperCase();
            if (uppercaseContext.contains("CHROMIUM") || uppercaseContext.contains("WEBVIEW")) {
                driver.context(context);
                return;
            }
        }
        throw new RuntimeException("No WEBVIEW/CHROMIUM context found: " + driver.getContextHandles());
    }

    public String getCurrentContext() {
        AndroidDriver driver = (AndroidDriver) unwrap(getDriver());
        return driver.getContext();
    }

    private WebDriver unwrap(WebDriver driver) {
        WebDriver unwrappedDriver = driver;
        while (unwrappedDriver instanceof WrapsDriver) {
            unwrappedDriver = ((WrapsDriver) unwrappedDriver).getWrappedDriver();
        }
        return unwrappedDriver;
    }
}