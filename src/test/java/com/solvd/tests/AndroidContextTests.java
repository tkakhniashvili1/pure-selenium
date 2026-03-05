package com.solvd.tests;

import com.solvd.pages.android.HomePage;
import com.zebrunner.carina.core.AbstractTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Set;

public class AndroidContextTests extends AbstractTest {

    private SoftAssert softly;

    @BeforeMethod
    public void initSoftAssert() {
        softly = new SoftAssert();
    }

    @Test
    public void verifyContextSwitchingInAndroidChrome() {
        HomePage homePage = initPage(getDriver(), HomePage.class);
        homePage.open();

        Set<String> contexts = homePage.getAvailableContexts();
        softly.assertTrue(contexts.contains("NATIVE_APP"), "NATIVE_APP context should exist.");
        softly.assertTrue(contexts.size() > 1, "Web context should exist in addition to native context.");

        homePage.triggerNativeRequiredActionInWeb();

        homePage.handleNativePopup();
        softly.assertEquals(homePage.getCurrentContext(), "NATIVE_APP", "Context should be NATIVE_APP after switching.");

        homePage.switchBackToWeb();
        softly.assertNotEquals(homePage.getCurrentContext(), "NATIVE_APP", "Context should switch back to web context.");

        softly.assertAll();
    }
}