package com.solvd.tests;

import com.solvd.utils.ConfigReader;
import com.solvd.utils.GridConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.URL;
import java.time.Duration;

public abstract class AbstractTest {

    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<String> TL_BROWSER = new ThreadLocal<>();

    protected WebDriver getDriver() {
        return TL_DRIVER.get();
    }

    protected String getBrowserName() {
        return TL_BROWSER.get();
    }

    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUpAndOpenBaseUrl(@Optional("chrome") String browser) {
        TL_BROWSER.set(browser);
        WebDriver d = createDriver(browser);

        d.manage().window().maximize();
        d.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("page.load.timeout")))
        );

        TL_DRIVER.set(d);

        d.manage().deleteAllCookies();
        d.get(ConfigReader.getProperty("base.url"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        WebDriver d = TL_DRIVER.get();
        try {
            if (!result.isSuccess()) {
                ScreenshotUtil.captureScreenshot(d, result.getName(), getBrowserName());
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        } finally {
            if (d != null) d.quit();
            TL_DRIVER.remove();
            TL_BROWSER.remove();
        }
    }

    private WebDriver createDriver(String browser) {
        if (browser == null) throw new RuntimeException("browser parameter is missing");

        try {
            if (GridConfigReader.useGrid()) {
                URL gridUrl = new URL(GridConfigReader.getProperty("grid.url"));

                switch (browser.toLowerCase()) {
                    case "chrome":
                        return new RemoteWebDriver(gridUrl, new ChromeOptions());
                    case "firefox":
                        return new RemoteWebDriver(gridUrl, new FirefoxOptions());
                    case "edge":
                        return new RemoteWebDriver(gridUrl, new EdgeOptions());
                    default:
                        throw new RuntimeException("Unsupported browser for grid: " + browser);
                }
            } else {
                switch (browser.toLowerCase()) {
                    case "chrome":
                        return new ChromeDriver();
                    case "firefox":
                        return new FirefoxDriver();
                    case "edge":
                        return new EdgeDriver();
                    default:
                        throw new RuntimeException("Unsupported browser: " + browser);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create driver for browser: " + browser, e);
        }
    }
}