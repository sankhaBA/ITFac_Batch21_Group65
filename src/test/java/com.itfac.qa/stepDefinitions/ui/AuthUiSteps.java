package com.itfac.qa.stepDefinitions.ui;

import com.itfac.qa.hooks.Hooks;
import com.itfac.qa.utils.ConfigurationManager;
import com.itfac.qa.utils.LoginHelper;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.junit.Assert;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AuthUiSteps {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();

    private WebDriver getDriver() {
        return Hooks.getDriver();
    }

    private WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(config.getExplicitWait()));
    }

    @Given("I open the application login page")
    public void openLoginPage() {
        LoginHelper.navigateToLoginPage();
    }

    @When("I enter username {string}")
    public void enterUsername(String username) {
        if (username != null && !username.isEmpty()) {
            LoginHelper.enterUsername(username);
        }
    }

    @When("I enter password {string}")
    public void enterPassword(String password) {
        if (password != null && !password.isEmpty()) {
            LoginHelper.enterPassword(password);
        }
    }

    @When("I click the login button")
    public void clickLogin() {
        LoginHelper.clickLoginButton();
    }

    @Then("I should be redirected to the Dashboard")
    public void verifyRedirect() {
        // Wait for URL to change
        getWait().until(ExpectedConditions.urlContains("dashboard"));
        String currentUrl = getDriver().getCurrentUrl();
        Assert.assertTrue("Not on dashboard!", currentUrl.contains("dashboard"));
    }

    @Then("I should see an error message saying {string}")
    public void verifyGlobalError(String errorMsg) {
        // Inspect the error message element on your app
        WebElement errorAlert = getWait()
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assert.assertTrue(errorAlert.getText().contains(errorMsg));
    }

    @Then("I should see a validation error {string} in red")
    public void verifyValidationError(String expectedMsg) {
        // Assuming validation errors have a class like 'text-danger' or 'error-msg'
        // You might need to adjust this xpath to find text anywhere on page if specific
        // ID isn't known
        boolean textPresent = getDriver().getPageSource().contains(expectedMsg);
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

    @Then("I should be redirected to the Login page")
    public void verifyLoginRedirect() {
        getWait().until(ExpectedConditions.urlContains("login"));
        Assert.assertTrue(getDriver().getCurrentUrl().contains("login"));
    }

    @Then("I should see a success message")
    public void verifySuccessMsg() {
        boolean successMsg = getDriver().getPageSource().contains("You have been logged out successfully."); // Adjust based
                                                                                                        // on actual
                                                                                                        // message
        Assert.assertTrue(successMsg);
    }
}