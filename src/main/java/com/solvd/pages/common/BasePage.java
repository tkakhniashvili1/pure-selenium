package com.solvd.pages.common;

import com.zebrunner.carina.utils.config.Configuration;
import com.zebrunner.carina.webdriver.decorator.ExtendedWebElement;
import com.zebrunner.carina.webdriver.gui.AbstractPage;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;

public abstract class BasePage extends AbstractPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePage.class);
    private boolean frontOfficeIframeEnsured = false;

    @FindBy(css = "iframe#framelive, iframe.framelive, iframe[name='framelive']")
    private ExtendedWebElement frontOfficeIframe;

    public BasePage(WebDriver driver) {
        super(driver);
    }

    protected void ensureFrontOfficeIframe(ExtendedWebElement probeElement) {
        WebDriver driver = getDriver();
        openBaseUrlIfNeeded(driver);
        driver.switchTo().defaultContent();

        long timeout = getDefaultWaitTimeout().getSeconds();

        waitUntil(d -> {
            if (isAnyElementDisplayed(probeElement)) return true;

            if (frontOfficeIframe.isPresent()) {
                try {
                    d.switchTo().frame(frontOfficeIframe.getElement());
                    return isAnyElementDisplayed(probeElement);
                } catch (StaleElementReferenceException e) {
                    d.switchTo().defaultContent();
                    return false;
                }
            }

            return false;
        }, timeout);
    }

    private void openBaseUrlIfNeeded(WebDriver driver) {
        String baseUrl = Configuration.getRequired("url");

        String currentUrl = null;
        try {
            currentUrl = driver.getCurrentUrl();
        } catch (WebDriverException e) {
            LOGGER.warn("Cannot get current URL, will navigate to base URL", e);
        }

        if (currentUrl == null || currentUrl.isBlank() || currentUrl.equals("about:blank") || currentUrl.startsWith("data:")) {
            driver.get(baseUrl);
        }
    }

    private boolean isAnyElementDisplayed(ExtendedWebElement... elements) {
        for (ExtendedWebElement element : elements) {
            if (element.isVisible()) {
                return true;
            }
        }
        return false;
    }

    protected final void ensureFrontOfficeIframeOnce(ExtendedWebElement probeElement) {
        if (frontOfficeIframeEnsured && probeElement != null && probeElement.isElementPresent(1)) {
            return;
        }

        ensureFrontOfficeIframe(probeElement);
        frontOfficeIframeEnsured = true;
    }

    public boolean isPageOpened() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getAvailableContexts() {
        Object target = unwrapDriverObject(getDriver());
        try {
            Method method = target.getClass().getMethod("getContextHandles");
            return (Set<String>) method.invoke(target);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get available contexts from driver: " + target.getClass(), e);
        }
    }

    public String getCurrentContext() {
        Object target = unwrapDriverObject(getDriver());
        try {
            Method method = target.getClass().getMethod("getContext");
            return (String) method.invoke(target);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot get current context from driver: " + target.getClass(), e);
        }
    }

    protected void switchToNativeContext() {
        switchContext("NATIVE_APP");
    }

    protected void switchToWebContext() {
        Set<String> contexts = getAvailableContexts();
        for (String context : contexts) {
            if (!"NATIVE_APP".equals(context)) {
                switchContext(context);
                return;
            }
        }
        throw new IllegalStateException("No web context found. Available contexts: " + contexts);
    }

    private void switchContext(String contextName) {
        Object target = unwrapDriverObject(getDriver());
        try {
            Method method = target.getClass().getMethod("context", String.class);
            method.invoke(target, contextName);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot switch context to '" + contextName + "' using driver: " + target.getClass(), e);
        }
    }

    private Object unwrapDriverObject(Object candidate) {
        Object current = candidate;

        for (int i = 0; i < 10 && current != null; i++) {
            Method wrappedMethod = findMethod(current.getClass(), "getWrappedDriver");
            if (wrappedMethod == null) {
                return current;
            }

            try {
                Object next = wrappedMethod.invoke(current);
                if (next == null || next == current) {
                    return current;
                }
                current = next;
            } catch (Exception e) {
                return current;
            }
        }

        return current;
    }

    private Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            Method method = type.getMethod(name, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}