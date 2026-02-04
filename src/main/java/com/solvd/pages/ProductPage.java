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

    @FindBy(css = "[data-testid='pdp-error'], [role='alert']")
    private List<WebElement> pdpAlerts;

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

    public void addCurrentProductToBag(String size) {
        selectSizeFromDropdownIfPresent(size);
        click(driver, wait, addToBagButton);
        wait.until(d -> isViewBagButtonVisible() || hasAnyAlert());
    }

    public boolean isLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
            WebElement title = driver.findElement(By.cssSelector("h1"));
            return title.isDisplayed() && !title.getText().trim().isEmpty();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
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

    public BagPage openBag() {
        click(driver, wait, viewBagButton);
        return new BagPage(driver);
    }

    public boolean isProductDetailsLoaded() {
        return isLoaded();
    }

    private void selectSizeFromDropdownIfPresent(String size) {
        try {
            WebElement chooseSize = driver.findElement(
                    By.cssSelector("div[role='combobox'][aria-labelledby='size-input-label']")
            );

            click(driver, wait, chooseSize);

            wait.until(d -> "true".equals(chooseSize.getAttribute("aria-expanded")));

            String listboxId = chooseSize.getAttribute("aria-controls");
            if (listboxId == null || listboxId.isBlank()) return;

            WebElement listbox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id(listboxId))
            );

            By optionBy = By.xpath(".//*[@role='option' and not(@aria-disabled='true')]");
            wait.until(d -> !listbox.findElements(optionBy).isEmpty());

            List<WebElement> options = listbox.findElements(optionBy);
            click(driver, wait, findSizeOption(options, size));
        } catch (NoSuchElementException | TimeoutException e) {
            throw e;
        }
    }

    private WebElement findSizeOption(List<WebElement> options, String size) {
        for (WebElement opt : options) {
            String dataValue = opt.getAttribute("data-value");
            String text = opt.getText();

            if (dataValue != null && dataValue.trim().equalsIgnoreCase(size)) return opt;
            if (text != null && text.trim().equalsIgnoreCase(size)) return opt;
        }
        throw new NoSuchElementException("Size option not found: " + size);
    }

    private boolean hasAnyAlert() {
        try {
            return pdpAlerts != null && pdpAlerts.stream().anyMatch(WebElement::isDisplayed);
        } catch (StaleElementReferenceException e) {
            return true;
        }
    }
}
