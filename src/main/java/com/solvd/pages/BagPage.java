package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.solvd.utils.UiActions.click;

public class BagPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//*[self::button or self::a][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'checkout')]")
    private WebElement checkoutButton;

    public BagPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        PageFactory.initElements(driver, this);
    }

    public CheckoutPage proceedToCheckout() {
        click(driver, wait, checkoutButton);
        return new CheckoutPage(driver);
    }
}
