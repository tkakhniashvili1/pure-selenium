package com.solvd.tests;

import com.solvd.pages.common.HomePageBase;
import com.solvd.utils.MobileContextUtils;
import com.zebrunner.carina.core.AbstractTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class AndroidContextTests extends AbstractTest {

    private SoftAssert softly;

    @BeforeMethod
    public void initSoftAssert() {
        softly = new SoftAssert();
    }

    @Test
    public void verifyContextSwitching() {
        MobileContextUtils contextUtils = new MobileContextUtils();

        HomePageBase homePage = initPage(getDriver(), HomePageBase.class);
        homePage.open();
        homePage.triggerNativeRequiredActionInWeb();

        contextUtils.switchMobileContext(MobileContextUtils.View.NATIVE);
        getDriver().navigate().back();
        contextUtils.switchMobileContext(MobileContextUtils.View.WEB_BROWSER);

        homePage.open();
        softly.assertTrue(getDriver().getCurrentUrl().contains("prestashop.com"),
                "Unexpected URL after switching back to WEB context: " + getDriver().getCurrentUrl());
        softly.assertAll();
    }
}