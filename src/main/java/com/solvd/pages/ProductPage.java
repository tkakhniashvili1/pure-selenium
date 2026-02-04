package com.solvd.pages;

import com.solvd.utils.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.solvd.utils.UiActions.click;

public class ProductPage {

    protected WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "h1")
    private WebElement pdpTitle;

    @FindBy(css = "[data-testid='item-form-addToBag-button']")
    private WebElement addToBagButton;

    @FindBy(xpath = "//*[self::a or self::button][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'view bag')]")
    private WebElement viewBagButton;

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        PageFactory.initElements(driver, this);
    }

    public void addCurrentProductToBag() {
        selectRandomSizeFromDropdownIfPresent();
        click(driver, wait, addToBagButton);
    }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(pdpTitle));
            return pdpTitle.isDisplayed() && !pdpTitle.getText().trim().isEmpty();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isViewBagButtonVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOf(viewBagButton));
            return viewBagButton.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public void openBag() {
        click(driver, wait, addToBagButton);
    }

    private void selectRandomSizeFromDropdownIfPresent() {
        try {
            WebElement chooseSize = driver.findElement(By.xpath(
                    "//*[(@role='combobox' or @aria-haspopup='listbox') and " +
                            "contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'choose size')]"
            ));
            click(driver, wait, chooseSize);

            wait.until(d -> !d.findElements(By.cssSelector("li[role='option'][data-value]")).isEmpty());
            List<WebElement> options = driver.findElements(By.cssSelector("li[role='option'][data-value]"));

            click(driver, wait, options.get((int) (Math.random() * options.size())));
        } catch (Exception ignored) {
        }
    }

    public boolean isProductDetailsLoaded() {
        return isLoaded();
    }
}
