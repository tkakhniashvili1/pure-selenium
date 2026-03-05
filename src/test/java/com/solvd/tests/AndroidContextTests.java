package com.solvd.tests;

import com.solvd.pages.common.HomePageBase;
import com.solvd.utils.MobileContextUtils;
import com.zebrunner.carina.core.AbstractTest;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.openqa.selenium.ContextAware;
import org.openqa.selenium.NotFoundException;
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
    public void verifyContextSwitching() {
        HomePageBase homePageBase = initPage(getDriver(), HomePageBase.class);
        MobileContextUtils context = new MobileContextUtils();

        homePageBase.open();
        homePageBase.triggerNativeRequiredActionInWeb();

        context.switchMobileContext(MobileContextUtils.View.NATIVE);
        getDriver().navigate().back();

        Set<String> handles = ((ContextAware) getDriver()).getContextHandles();
        String webContext = handles.stream()
                .filter(h -> !"NATIVE_APP".equals(h))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No WEB context"));

        ((SupportsContextSwitching) getDriver()).context(webContext);

        String currentContext = ((SupportsContextSwitching) getDriver()).getContext();
        softly.assertNotEquals(currentContext, "NATIVE_APP", "Should be in WEB context.");
        softly.assertTrue(getDriver().getCurrentUrl().contains("prestashop"), "Unexpected URL after switching back to WEB.");
        softly.assertAll();
    }
}