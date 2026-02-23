package com.solvd.pages.android;

import com.solvd.pages.common.SearchResultsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.ANDROID_PHONE, parentClass = SearchResultsPageBase.class)
public class SearchResultsPage extends SearchResultsPageBase {
    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }
}