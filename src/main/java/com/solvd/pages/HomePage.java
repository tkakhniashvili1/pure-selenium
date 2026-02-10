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
import static com.solvd.utils.UiActions.clickIfPresent;

public class HomePage {

    protected final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "iframe#framelive")
    private List<WebElement> iframes;

    @FindBy(css = "#search_widget input[name='s']")
    private WebElement searchInput;

    @FindBy(css = "#search_widget button[type='submit']")
    private WebElement searchSubmitButton;

    @FindBy(css = "#content .product-title a")
    private List<WebElement> productTitleLinks;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(Integer.parseInt(ConfigReader.getProperty("implicit.wait"))));
        PageFactory.initElements(driver, this);
    }

    public SearchResultsPage search(String query) {
        ensureFrontOfficeIframe();

        click(driver, wait, searchInput);
        searchInput.sendKeys(Keys.chord(Keys.COMMAND, "a"), Keys.BACK_SPACE, query);

        if (!clickIfPresent(driver, wait, searchSubmitButton)) {
            searchInput.sendKeys(Keys.ENTER);
        }

        return new SearchResultsPage(driver);
    }

    public String getSearchKeywordFromHome() {
        ensureFrontOfficeIframe();

        wait.until(d -> !productTitleLinks.isEmpty()
                && !productTitleLinks.get(0).getText().trim().isEmpty());

        String title = productTitleLinks.get(0).getText().trim();

        String[] tokens = title.split("[^A-Za-z0-9]+");
        for (String t : tokens) {
            if (t.length() >= 4) return t.toLowerCase();
        }
        return title.substring(0, Math.min(6, title.length())).toLowerCase();
    }

    private void ensureFrontOfficeIframe() {
        try {
            wait.until(ExpectedConditions.visibilityOf(searchInput));
            return;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException ignored) {}

        driver.switchTo().defaultContent();
        wait.until(d -> !iframes.isEmpty());

        WebElement target = iframes.get(0);
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(target));
        wait.until(ExpectedConditions.visibilityOf(searchInput));
    }

    public ProductPage openFirstProduct() {
        ensureFrontOfficeIframe();

        wait.until(d -> !productTitleLinks.isEmpty());
        WebElement first = productTitleLinks.stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No displayed home product"));

        click(driver, wait, first);
        return new ProductPage(driver);
    }
}