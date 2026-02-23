package com.solvd.pages.android;

import com.solvd.pages.common.HomePageBase;
import com.solvd.pages.common.ProductPageBase;
import com.solvd.pages.common.SearchResultsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = HomePageBase.class)
public class HomePage extends HomePageBase {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#search_widget input[name='s']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }

    @Override
    public String getSearchKeywordFromHome() {
        waitForPageOpened();
        return "dress";
    }

    @Override
    public ProductPageBase openFirstProduct() {
        SearchResultsPageBase results = search(getSearchKeywordFromHome());
        return results.openFirstVisibleProduct();
    }
}