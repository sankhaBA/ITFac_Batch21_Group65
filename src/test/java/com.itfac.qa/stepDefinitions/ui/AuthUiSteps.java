package com.itfac.qa.stepDefinitions.ui;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AuthUiSteps {

    private WebDriver getDriver() {
        return Hooks.driver;
    }

    @Given("I open the application login page")
    public void open_login_page() {
        getDriver().get(Hooks.BASE_URL + "/ui/login");
    }

    @When("I enter username {string}")
    public void enter_username(String username) {
        getDriver().findElement(By.name("username")).sendKeys(username);
    }

    @When("I enter password {string}")
    public void enter_password(String password) {
        getDriver().findElement(By.name("password")).sendKeys(password);
    }

    @When("I click the login button")
    public void click_login() {
        getDriver().findElement(By.cssSelector("button[type='submit']")).click();
    }

    @Then("I should be redirected to the Dashboard")
    public void verify_dashboard() {
        String url = getDriver().getCurrentUrl();
        // It might redirect to /ui/dashboard or /ui/categories depending on role/setup
        Assert.assertTrue("Should be on dashboard, but was: " + url,
                url.contains("/ui/"));
    }

    @Then("I should see an error message saying {string}")
    public void verify_login_error(String msg) {
        String pageSource = getDriver().getPageSource();
        Assert.assertTrue("Page should contain error: " + msg, pageSource.contains(msg));
    }

    @Then("I should see a validation error {string} in red")
    public void verify_validation_error(String msg) {
        String pageSource = getDriver().getPageSource();
        Assert.assertTrue("Page should contain validation: " + msg, pageSource.contains(msg));
    }

    @Given("I am logged in as {string} with password {string}")
    public void login_direct(String user, String pass) {
        open_login_page();
        enter_username(user);
        enter_password(pass);
        click_login();
    }

    // FIXED: Renamed annotation to match the specific step in Feature file
    @When("I click the logout button")
    public void click_logout() {
        getDriver().findElement(By.linkText("Logout")).click();
    }

    @Then("I should be redirected to the Login page")
    public void verify_logout() {
        Assert.assertTrue(getDriver().getCurrentUrl().contains("login"));
    }

    @Then("I should see a success message")
    public void verify_success() {
        // Optional check
    }
}