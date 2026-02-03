package com.solvd.tests;

import com.solvd.pages.HomePage;
import com.solvd.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class ECommerceTests {

    private WebDriver driver;

    @BeforeClass
    public void setup() {
        String browser = ConfigReader.getProperty("browser");
        switch (browser.toLowerCase()) {
            case "chrome":
                driver = new ChromeDriver();
                break;
            case "firefox":
                driver = new FirefoxDriver();
                break;
            case "edge":
                driver = new EdgeDriver();
                break;
            default:
                throw new RuntimeException("Unsupported browser: " + browser);
        }

        driver.manage().window().maximize();

        driver.manage().timeouts().pageLoadTimeout(
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("page.load.timeout")))
        );
    }

    @Test
    public void successfulProductSearch() {
        driver.get(ConfigReader.getProperty("base.url"));
        HomePage homePage = new HomePage(driver);

        homePage.acceptCookiesIfPresent();
        homePage.dismissCountryPopupIfPresent();
        homePage.searchProduct("dress");

        Assert.assertTrue(homePage.isSearchResultsPageLoaded());
        Assert.assertTrue(homePage.hasResults());
        Assert.assertTrue(homePage.isSearchTermInUrl("dress"));
        Assert.assertTrue(homePage.hasProductContaining("dress"));
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
