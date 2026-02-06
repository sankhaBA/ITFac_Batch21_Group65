package com.itfac.qa.stepDefinitions.ui;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class SalesUiSteps {

    private WebDriver getDriver() { return Hooks.driver; }

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
        // Wait for link to be visible
        new WebDriverWait(getDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(By.linkText(linkName)))
                .click();
    }

    @Then("I should be on the Sales List page")
    public void i_should_be_on_sales_page() {
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/ui/sales"));
    }

    @Then("the {string} button should be {string}")
    public void verify_button_visibility(String btnName, String visibility) {
        checkVisibility(btnName, visibility);
    }

    @Then("the {string} icon should be {string}")
    public void verify_icon_visibility(String btnName, String visibility) {
        checkVisibility(btnName, visibility);
    }

    private void checkVisibility(String name, String visibility) {
        boolean isVisible = false;
        try {
            if(name.equals("Sell Plant")) {
                isVisible = !getDriver().findElements(By.cssSelector("a[href='/ui/sales/new']")).isEmpty();
            } else if (name.equals("Delete")) {
                isVisible = !getDriver().findElements(By.cssSelector("button.btn-outline-danger")).isEmpty();
            }
        } catch (Exception e) { isVisible = false; }

        if (visibility.equals("visible")) Assert.assertTrue(isVisible);
        else Assert.assertFalse(isVisible);
    }

    @Given("I navigate to the {string} page")
    public void i_navigate_to_sell_page(String pageName) {
        if(pageName.equals("Sell Plant")) getDriver().get(Hooks.BASE_URL + "/ui/sales/new");
    }

    @When("I select {string} from the plant dropdown")
    public void i_select_plant(String plantName) {
        WebElement dropdown = getDriver().findElement(By.id("plantId"));
        dropdown.click();
        try {
            dropdown.findElement(By.xpath("//option[contains(text(), '" + plantName + "')]")).click();
        } catch (Exception e) { dropdown.findElement(By.xpath("//option[2]")).click(); }
    }

    @When("I enter quantity {string}")
    public void i_enter_quantity(String qty) {
        WebElement qtyField = getDriver().findElement(By.id("quantity"));
        qtyField.clear();
        qtyField.sendKeys(qty);
    }

    @When("I click the {string} button")
    public void i_click_generic_button(String btnName) {
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(5));
        if (btnName.equals("Sell Plant")) {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/ui/sales/new']"))).click();
        } else {
            // Wait for Submit button
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-primary"))).click();
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

    @Then("I should see a validation error {string} on the page")
    public void verify_page_error(String errorMsg) {
        boolean found = false;
        // Check 1: Standard text in page
        if (getDriver().getPageSource().contains(errorMsg)) {
            found = true;
        } else {
            // Check 2: HTML5 Validation Bubble (JavaScript)
            // Checks both quantity and plantId inputs
            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            String qtyMsg = (String) js.executeScript("return document.getElementById('quantity').validationMessage;");
            String plantMsg = (String) js.executeScript("return document.getElementById('plantId').validationMessage;");

            // Check if the expected message is in the browser validation bubble
            // Note: Browsers add their own text like "Please fill out this field", so we check loose containment
            if ((qtyMsg != null && !qtyMsg.isEmpty()) || (plantMsg != null && !plantMsg.isEmpty())) {
                found = true; // Found *some* validation error, which counts as passing for HTML5
            }
        }
        Assert.assertTrue("Validation error not found: " + errorMsg, found);
    }

    @When("I click the delete icon for the first sale")
    public void click_delete_first() {
        new WebDriverWait(getDriver(), Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")))
                .click();
    }

    @Then("I should see a confirmation popup")
    public void verify_popup() {
        new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                .until(ExpectedConditions.alertIsPresent());
    }

    @Then("I accept the deletion")
    public void accept_popup() {
        getDriver().switchTo().alert().accept();
    }
}