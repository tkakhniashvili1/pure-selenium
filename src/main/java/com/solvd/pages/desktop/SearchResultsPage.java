package com.solvd.pages.desktop;

import com.solvd.pages.common.SearchResultsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = SearchResultsPageBase.class)
public class SearchResultsPage extends SearchResultsPageBase {
    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }
}