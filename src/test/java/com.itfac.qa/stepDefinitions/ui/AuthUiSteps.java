package com.itfac.qa.steps;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.junit.Assert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AuthUiSteps {

    WebDriver driver = Hooks.driver;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

    @Given("I open the application login page")
    public void openLoginPage() {
        driver.get("http://localhost:8080/ui/login");
    }

    @When("I enter username {string}")
    public void enterUsername(String username) {
        if (username != null && !username.isEmpty()) {
            // CHANGED: used By.name("username") because ID is missing
            WebElement userField = driver.findElement(By.name("username"));
            userField.clear();
            userField.sendKeys(username);
        }
    }

    @When("I enter password {string}")
    public void enterPassword(String password) {
        if (password != null && !password.isEmpty()) {
            // CHANGED: used By.name("password") because ID is missing
            WebElement passField = driver.findElement(By.name("password"));
            passField.clear();
            passField.sendKeys(password);
        }
    }

    @When("I click the login button")
    public void clickLogin() {
        // CHANGED: used CSS Selector to find the button with type="submit"
        // Alternative: By.xpath("//button[contains(text(), 'Login')]")
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Then("I should be redirected to the Dashboard")
    public void verifyRedirect() {
        // Wait for URL to change
        wait.until(ExpectedConditions.urlContains("dashboard"));
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue("Not on dashboard!", currentUrl.contains("dashboard"));
    }

    @Then("I should see an error message saying {string}")
    public void verifyGlobalError(String errorMsg) {
        // Inspect the error message element on your app
        WebElement errorAlert = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assert.assertTrue(errorAlert.getText().contains(errorMsg));
    }

    @Then("I should see a validation error {string} in red")
    public void verifyValidationError(String expectedMsg) {
        // Assuming validation errors have a class like 'text-danger' or 'error-msg'
        // You might need to adjust this xpath to find text anywhere on page if specific
        // ID isn't known
        boolean textPresent = driver.getPageSource().contains(expectedMsg);
        Assert.assertTrue("Validation message not found!", textPresent);
    }

    // --- Logout Steps ---

    @Given("I am logged in as {string} with password {string}")
    public void loginPrecondition(String user, String pass) {
        openLoginPage();
        enterUsername(user);
        enterPassword(pass);
        clickLogin();
        verifyRedirect();
    }

    @When("I click the {string} button")
    public void clickLogout(String btnName) {
        // For Logout link: use href attribute since text is split by icon element
        if (btnName.equalsIgnoreCase("Logout")) {
            driver.findElement(By.xpath("//a[@href='/ui/logout']")).click();
        } else {
            // Fallback for other buttons: use contains on all descendant text
            driver.findElement(By.xpath("//*[contains(., '" + btnName + "')]")).click();
        }
    }

    @Then("I should be redirected to the Login page")
    public void verifyLoginRedirect() {
        wait.until(ExpectedConditions.urlContains("login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("login"));
    }

    @Then("I should see a success message")
    public void verifySuccessMsg() {
        boolean successMsg = driver.getPageSource().contains("You have been logged out successfully."); // Adjust based
                                                                                                        // on actual
                                                                                                        // message
        Assert.assertTrue(successMsg);
    }
}