package com.solvd.tests;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public abstract class AbstractTest {

    private static final ThreadLocal<WebDriver> TL_DRIVER = new ThreadLocal<>();

    protected WebDriver getDriver() {
        return TL_DRIVER.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpAndOpenBaseUrl() {
        WebDriver d = createDriver(ConfigReader.getProperty("browser"));

        d.manage().window().maximize();
        d.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("page.load.timeout")))
        );

        TL_DRIVER.set(d);

        d.manage().deleteAllCookies();
        d.get(ConfigReader.getProperty("base.url"));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver d = TL_DRIVER.get();
        try {
            if (d != null) d.quit();
        } finally {
            TL_DRIVER.remove();
        }
    }

    private WebDriver createDriver(String browser) {
        if (browser == null) throw new RuntimeException("browser property is missing");

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
}
