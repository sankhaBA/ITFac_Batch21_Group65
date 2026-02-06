package com.itfac.qa.stepDefinitions.ui;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor; // Added for robust clicking
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CategoryUiSteps {

    WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(15));
    String BASE_URL = "http://localhost:8080/ui";

    // --- CREDENTIALS ---
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "admin123";
    private final String REGULAR_USER = "testuser";
    private final String REGULAR_PASS = "test123";

    // --- LOCATORS ---

    By usernameField = By.xpath("//input[@name='username']");
    By passwordField = By.xpath("//input[@name='password']");
    By loginBtn = By.xpath("//button[contains(text(),'Login') or contains(text(),'Sign')]");
    By cancelBtn = By.xpath("//a[contains(text(),'Cancel')] | //button[contains(text(),'Cancel')]");
    By logoutBtn = By.xpath("//*[contains(text(),'Logout')] | //*[@title='Logout'] | //a[contains(@href,'logout')]");
    By menuCategories = By.cssSelector("a[href*='categories']");
    By searchInput = By.cssSelector("input[placeholder*='Search']");
    By searchBtn = By.xpath("//button[contains(text(),'Search')]");
    By nameInput = By.cssSelector("input[id='name'], input[name='name']");
    By saveBtn = By.xpath("//button[text()='Save'] | //input[@type='submit']");
    By errorLocator = By.className("invalid-feedback");

    @Given("I open the application")
    public void i_open_the_application() {
        Hooks.driver.get(BASE_URL + "/login");
    }

    @Given("I am logged in as {string}")
    public void i_am_logged_in_as_role(String role) {
        String user = role.equalsIgnoreCase("Admin") ? ADMIN_USER : REGULAR_USER;
        String pass = role.equalsIgnoreCase("Admin") ? ADMIN_PASS : REGULAR_PASS;
        performLogin(user, pass);
    }

    private void performLogin(String user, String pass) {
        try {
            if (Hooks.driver.findElements(usernameField).size() > 0) {
                wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).sendKeys(user);
                Hooks.driver.findElement(passwordField).sendKeys(pass);
                Hooks.driver.findElement(loginBtn).click();
            }
        } catch (Exception e) {
        }
    }

    @Given("I navigate to the {string} page")
    public void i_navigate_to_page(String pageName) {
        if (pageName.equalsIgnoreCase("Categories")) {
            wait.until(ExpectedConditions.elementToBeClickable(menuCategories)).click();
        }
    }

    @When("I click the {string} button")
    public void i_click_button(String btnName) {
        By locator = null;

        if (btnName.equals("Add Category")) {
            locator = By.xpath("//a[contains(@class,'btn-primary') and contains(text(),'Add')]");
        } else if (btnName.equals("Save")) {
            locator = saveBtn;
        } else if (btnName.equals("Logout")) {
            locator = logoutBtn;
        } else if (btnName.equals("Search")) {
            locator = searchBtn;
        } else if (btnName.equals("Cancel")) {
            locator = cancelBtn;
        }

        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e) {
            // Fallback: If standard click fails (e.g. obscured), use Javascript Force Click
            System.out.println("Standard click failed for " + btnName + ". Attempting Force Click.");
            WebElement element = Hooks.driver.findElement(locator);
            ((JavascriptExecutor) Hooks.driver).executeScript("arguments[0].click();", element);
        }
    }

    @When("I enter {string} in the category name field")
    public void i_enter_name(String name) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        input.clear();
        input.sendKeys(name);
    }

    @Then("I should see a success message {string}")
    public void i_should_see_success(String msg) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + msg + "')]")));
    }

    @Then("I should see a validation error {string}")
    public void i_should_see_error(String expectedError) {
        WebElement errorElem = wait.until(ExpectedConditions.visibilityOfElementLocated(errorLocator));
        String actualError = errorElem.getText().trim();
        String expectedClean = expectedError.replace(".", "").trim();
        String actualClean = actualError.replace(".", "").trim();
        Assert.assertTrue("Error mismatch! Found: " + actualError, actualClean.contains(expectedClean));
    }

    @Then("I should see {string} in the category list")
    public void i_should_see_in_list(String catName) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), catName));
    }

    @When("I click the {string} button for the category {string}")
    public void i_click_action_for_category(String action, String catName) {
        String xpath = "";
        if (action.equalsIgnoreCase("Edit")) {
            xpath = "//tr[contains(., '" + catName + "')]//a[@title='Edit']";
        } else if (action.equalsIgnoreCase("Delete")) {
            xpath = "//tr[contains(., '" + catName + "')]//button[@title='Delete']";
        }
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
    }

    @When("I accept the delete confirmation")
    public void i_accept_alert() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) {
            try {
                Hooks.driver.findElement(By.xpath("//button[text()='Yes' or text()='Confirm' or text()='OK']")).click();
            } catch (Exception ex) {
            }
        }
    }

    @When("I enter {string} in the search box")
    public void i_enter_search(String term) {
        WebElement search = Hooks.driver.findElement(searchInput);
        search.clear();
        search.sendKeys(term);
    }

    @When("I click the search button")
    public void i_click_search() {
        Hooks.driver.findElement(searchBtn).click();
    }

    @Then("I should not see the {string} button")
    public void i_should_not_see_button(String btnName) {
        By locator = By.xpath("//a[contains(@class,'btn-primary') and contains(text(),'Add')]");
        List<WebElement> buttons = Hooks.driver.findElements(locator);
        if (!buttons.isEmpty()) {
            Assert.assertFalse("Add button is visible!", buttons.get(0).isDisplayed());
        }
    }

    @Then("I should not see the {string} buttons")
    public void i_should_not_see_delete_buttons(String btnName) {
        List<WebElement> deletes = Hooks.driver.findElements(By.xpath("//button[@title='Delete']"));
        for (WebElement btn : deletes) {
            // It passes if the button is either NOT displayed OR it IS displayed but
            // DISABLED
            boolean isHidden = !btn.isDisplayed();
            boolean isDisabled = btn.getAttribute("disabled") != null;

            Assert.assertTrue("Delete button is visible and active!", isHidden || isDisabled);
        }
    }

    @Then("I should see the {string} button")
    public void i_should_see_button(String btnName) {
        if (btnName.equals("Add Category")) {
            // Re-using the Add Category locator to check for visibility
            By addBtn = By.xpath("//a[contains(@class,'btn-primary') and contains(text(),'Add')]");
            WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(addBtn));
            Assert.assertTrue("Add Category button should be visible!", btn.isDisplayed());
        }
    }
}