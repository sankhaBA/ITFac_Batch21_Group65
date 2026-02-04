package com.itfac.qa.stepDefinitions.ui;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CategoryUiSteps {

    WebDriverWait wait = new WebDriverWait(Hooks.driver, Duration.ofSeconds(10));
    String BASE_URL = "http://localhost:8080/ui"; // Adjust port if needed

    // Locators
    By usernameField = By.id("username");
    By passwordField = By.id("password");
    By loginBtn = By.xpath("//button[text()='Login']");
    By logoutBtn = By.xpath("//button[text()='Logout']");

    By menuCategories = By.xpath("//a[@href='/ui/categories']");
    By addCategoryBtn = By.xpath("//button[contains(text(),'Add Category')]");
    By nameInput = By.id("name"); // Assuming ID from standard practices
    By saveBtn = By.xpath("//button[text()='Save']");
    By successMessage = By.id("success-msg"); // Adjust ID based on your inspecting
    By errorMessage = By.className("invalid-feedback");
    By searchInput = By.id("search");
    By searchBtn = By.xpath("//button[text()='Search']");

    @Given("I open the application")
    public void i_open_the_application() {
        Hooks.driver.get(BASE_URL + "/login");
    }

    @Given("I am logged in as {string} with password {string}")
    public void i_am_logged_in_as(String user, String pass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField)).sendKeys(user);
        Hooks.driver.findElement(passwordField).sendKeys(pass);
        Hooks.driver.findElement(loginBtn).click();
    }

    @Given("I navigate to the {string} page")
    public void i_navigate_to_page(String pageName) {
        if (pageName.equalsIgnoreCase("Categories")) {
            wait.until(ExpectedConditions.elementToBeClickable(menuCategories)).click();
        }
    }

    @When("I click the {string} button")
    public void i_click_button(String btnName) {
        if (btnName.equals("Add Category")) {
            wait.until(ExpectedConditions.elementToBeClickable(addCategoryBtn)).click();
        } else if (btnName.equals("Save")) {
            Hooks.driver.findElement(saveBtn).click();
        } else if (btnName.equals("Logout")) {
            Hooks.driver.findElement(logoutBtn).click();
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
        // Handling checking for toast message or specific element text
        // Note: You might need to inspect the element ID for the toast message
        WebElement msgElement = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + msg + "')]")));
        Assert.assertTrue(msgElement.isDisplayed());
    }

    @Then("I should see a validation error {string}")
    public void i_should_see_error(String errorMsg) {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
        Assert.assertEquals(errorMsg, error.getText());
    }

    @Then("I should see {string} in the category list")
    public void i_should_see_in_list(String catName) {
        // Simple check if the text exists in the table
        boolean found = Hooks.driver.getPageSource().contains(catName);
        Assert.assertTrue("Category " + catName + " not found in list", found);
    }

    @When("I click the {string} button for the category {string}")
    public void i_click_action_for_category(String action, String catName) {
        // Dynamic XPath to find the Edit/Delete button relative to the Category Name
        // Structure assumption: <tr><td>Name</td><td><button>Edit</button></td></tr>
        String xpath = "//td[text()='" + catName + "']/following-sibling::td/button[contains(text(),'" + action + "')]";
        Hooks.driver.findElement(By.xpath(xpath)).click();
    }

    @When("I accept the delete confirmation")
    public void i_accept_alert() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) {
            // Sometimes confirmation is a modal, not an alert.
            // If using a modal, you would click the "Confirm" button here.
        }
    }

    @When("I enter {string} in the search box")
    public void i_enter_search(String term) {
        Hooks.driver.findElement(searchInput).sendKeys(term);
    }

    @When("I click the search button")
    public void i_click_search() {
        Hooks.driver.findElement(searchBtn).click();
    }

    @Then("I should not see the {string} button")
    public void i_should_not_see_button(String btnName) {
        List<WebElement> buttons = Hooks.driver.findElements(addCategoryBtn);
        Assert.assertTrue("Button should not be present", buttons.isEmpty() || !buttons.get(0).isDisplayed());
    }

    @Then("I should not see the {string} buttons")
    public void i_should_not_see_delete_buttons(String btnName) {
        // Assuming delete buttons have a specific class or text
        List<WebElement> deletes = Hooks.driver.findElements(By.xpath("//button[contains(text(),'Delete')]"));
        Assert.assertTrue("Delete buttons should not be visible", deletes.isEmpty());
    }
}