package com.solvd.pages.desktop;

import com.solvd.pages.common.SearchResultsPageBase;
import com.zebrunner.carina.utils.factory.DeviceType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@DeviceType(pageType = DeviceType.Type.DESKTOP, parentClass = SearchResultsPageBase.class)
public class SearchResultsPage extends SearchResultsPageBase {

    private static final By PAGE_READY_LOCATOR = By.cssSelector("#js-product-list");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected By getPageReadyLocator() {
        return PAGE_READY_LOCATOR;
    }
}
