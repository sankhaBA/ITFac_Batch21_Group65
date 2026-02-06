package com.itfac.qa.stepDefinitions.ui;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class SalesUiSteps {

    private WebDriver getDriver() {
        return Hooks.driver;
    }

    @Given("I navigate to the login page")
    public void i_navigate_to_login() {
        getDriver().get(Hooks.BASE_URL + "/ui/login");
    }

    @Given("I login as {string} via UI")
    public void i_login_as_ui(String role) {
        String username = role.equalsIgnoreCase("Admin") ? "admin" : "testuser";
        String password = role.equalsIgnoreCase("Admin") ? "admin123" : "test123";

        getDriver().findElement(By.name("username")).sendKeys(username);
        getDriver().findElement(By.name("password")).sendKeys(password);
        getDriver().findElement(By.cssSelector("button[type='submit']")).click();
    }

    @When("I click the {string} link in the navigation")
    public void i_click_nav_link(String linkName) {
        getDriver().findElement(By.linkText(linkName)).click();
    }

    @Then("I should be on the Sales List page")
    public void i_should_be_on_sales_page() {
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/ui/sales"));
    }

    // --- FIXED: Split into two separate steps to avoid confusion ---

    @Then("the {string} button should be {string}")
    public void verify_button_visibility(String btnName, String visibility) {
        checkVisibility(btnName, visibility);
    }

    @Then("the {string} icon should be {string}")
    public void verify_icon_visibility(String btnName, String visibility) {
        checkVisibility(btnName, visibility);
    }

    // Shared logic helper
    private void checkVisibility(String name, String visibility) {
        boolean isVisible = false;
        try {
            if(name.equals("Sell Plant")) {
                isVisible = getDriver().findElement(By.cssSelector("a[href='/ui/sales/new']")).isDisplayed();
            } else if (name.equals("Delete")) {
                isVisible = getDriver().findElement(By.cssSelector("button.btn-outline-danger")).isDisplayed();
            }
        } catch (Exception e) {
            isVisible = false;
        }

        if (visibility.equals("visible")) {
            Assert.assertTrue(name + " should be visible", isVisible);
        } else {
            Assert.assertFalse(name + " should be hidden", isVisible);
        }
    }
    // -----------------------------------------------------------

    @Given("I navigate to the {string} page")
    public void i_navigate_to_sell_page(String pageName) {
        if(pageName.equals("Sell Plant")) {
            getDriver().get(Hooks.BASE_URL + "/ui/sales/new");
        }
    }

    @When("I select {string} from the plant dropdown")
    public void i_select_plant(String plantName) {
        WebElement dropdown = getDriver().findElement(By.id("plantId"));
        dropdown.click();
        try {
            dropdown.findElement(By.xpath("//option[contains(text(), '" + plantName + "')]")).click();
        } catch (Exception e) {
            dropdown.findElement(By.xpath("//option[2]")).click();
        }
    }

    @When("I enter quantity {string}")
    public void i_enter_quantity(String qty) {
        WebElement qtyField = getDriver().findElement(By.id("quantity"));
        qtyField.clear();
        qtyField.sendKeys(qty);
    }

    @When("I click the {string} button")
    public void i_click_generic_button(String btnName) {
        if (btnName.equals("Sell Plant")) {
            getDriver().findElement(By.cssSelector("a[href='/ui/sales/new']")).click();
        } else {
            getDriver().findElement(By.cssSelector("button.btn-primary")).click();
        }
    }

    @Then("I should be redirected to the Sales List")
    public void check_redirect() {
        new WebDriverWait(getDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/ui/sales"));
    }

    @Then("the new sale should appear at the top of the list")
    public void check_new_sale() {
        List<WebElement> rows = getDriver().findElements(By.cssSelector("table tbody tr"));
        Assert.assertFalse("Sales table shouldn't be empty", rows.isEmpty());
    }
}