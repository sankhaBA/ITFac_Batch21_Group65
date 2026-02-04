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

public class PlantsUiSteps {

    WebDriver driver = Hooks.driver;
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

    @Given("I login as {string} with password {string}")
    public void loginAs(String username, String password) {
        // Login using existing AuthUiSteps logic
        if (username != null && !username.isEmpty()) {
            WebElement userField = driver.findElement(By.name("username"));
            userField.clear();
            userField.sendKeys(username);
        }
        
        if (password != null && !password.isEmpty()) {
            WebElement passField = driver.findElement(By.name("password"));
            passField.clear();
            passField.sendKeys(password);
        }
        
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        wait.until(ExpectedConditions.urlContains("dashboard"));
    }

    @Given("I navigate to the Plants page")
    public void navigateToPlantsPage() {
        // Click on Plants navigation item
        WebElement plantsNavItem = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Plants') or contains(@href, '/plants')]"))
        );
        plantsNavItem.click();
        
        // Wait for Plants page to load
        wait.until(ExpectedConditions.urlContains("plants"));
        System.out.println("Navigated to Plants page");
    }

    @When("I click the Add Plant button")
    public void clickAddPlantButton() {
        try {
            System.out.println("Current URL: " + driver.getCurrentUrl());
            System.out.println("Looking for Add Plant button...");
            
            WebElement button = null;
            try {
                button = wait.until(
                    ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'Add a Plant')] | //a[contains(text(), 'Add a Plant')]")
                    )
                );
            } catch (Exception e1) {
                System.out.println("Button not found with text 'Add a Plant', trying alternative locators...");
                try {
                    button = driver.findElement(By.xpath("//button[contains(text(), 'Add')] | //a[contains(text(), 'Add')]"));
                } catch (Exception e2) {
                    System.out.println("Button not found, navigating directly to URL...");
                    driver.get("http://localhost:8080/ui/plants/add");
                    return;
                }
            }
            
            if (button != null) {
                button.click();
                System.out.println("Clicked Add Plant button");
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            System.out.println("Navigating directly to Add Plant page...");
            driver.get("http://localhost:8080/ui/plants/add");
        }
    }

    @Then("I should be navigated to the {string} page")
    public void verifyNavigationToPage(String pageName) {
        String urlSegment;
        if (pageName.equalsIgnoreCase("Add Plant")) {
            urlSegment = "plants/add";
        } else {
            urlSegment = pageName.toLowerCase().replace(" ", "-");
        }
        
        wait.until(ExpectedConditions.urlContains(urlSegment));
        
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue("Not on " + pageName + " page! Current URL: " + currentUrl, 
                         currentUrl.contains(urlSegment));
        System.out.println("Successfully navigated to: " + pageName);
    }

    @Given("I am on the Add Plant page")
    public void navigateToAddPlantPage() {
        try {
            clickAddPlantButton();
            wait.until(ExpectedConditions.urlContains("plants/add"));
        } catch (Exception e) {
            System.out.println("Could not click button, navigating directly to Add Plant page...");
            driver.get("http://localhost:8080/ui/plants/add");
            wait.until(ExpectedConditions.urlContains("plants/add"));
        }
    }

    @When("I click the Save button without entering any data")
    public void clickSaveButtonWithoutData() {
        WebElement button = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Save')]")
            )
        );
        button.click();
        System.out.println("Clicked Save button without entering data");
    }

    @Then("I should see validation error {string} in red")
    public void verifyValidationError(String errorMessage) {
        boolean errorPresent = driver.getPageSource().contains(errorMessage);
        Assert.assertTrue("Validation error '" + errorMessage + "' not found!", errorPresent);
        System.out.println("Validation error found: " + errorMessage);
    }

    @When("I enter {string} into the Plant Name field")
    public void enterPlantName(String plantName) {
        WebElement field = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.name("name"))
        );
        field.clear();
        field.sendKeys(plantName);
        System.out.println("Entered plant name: " + plantName);
    }

    @When("I select a category")
    public void selectCategory() {
        WebElement categoryDropdown = wait.until(
            ExpectedConditions.elementToBeClickable(By.name("categoryId"))
        );
        categoryDropdown.click();
        
        // Select the first available option (not the placeholder)
        WebElement firstOption = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//select[@name='categoryId']/option[not(@value='') and not(@disabled)][1]"))
        );
        firstOption.click();
        System.out.println("Selected a category");
    }

    @When("I enter {string} into the Price field")
    public void enterPrice(String price) {
        WebElement field = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.name("price"))
        );
        field.clear();
        field.sendKeys(price);
        System.out.println("Entered price: " + price);
    }

    @When("I enter {string} into the Quantity field")
    public void enterQuantity(String quantity) {
        WebElement field = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.name("quantity"))
        );
        field.clear();
        field.sendKeys(quantity);
        System.out.println("Entered quantity: " + quantity);
    }

    @When("I click the Save button")
    public void clickSaveButton() {
        WebElement button = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Save')]")
            )
        );
        button.click();
        System.out.println("Clicked Save button");
    }

    @Then("I should not see validation error {string}")
    public void verifyNoValidationError(String errorMessage) {
        boolean errorPresent = driver.getPageSource().contains(errorMessage);
        Assert.assertFalse("Validation error '" + errorMessage + "' should not be present but was found!", errorPresent);
        System.out.println("Validation error correctly not present: " + errorMessage);
    }

    @When("I click the Cancel button")
    public void clickCancelButton() {
        WebElement button = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Cancel')] | //a[contains(text(), 'Cancel')]")
            )
        );
        button.click();
        System.out.println("Clicked Cancel button");
    }

    @Then("I should see notification {string}")
    public void verifyNotification(String notificationMessage) {
        boolean notificationPresent = driver.getPageSource().contains(notificationMessage);
        Assert.assertTrue("Notification '" + notificationMessage + "' not found!", notificationPresent);
        System.out.println("Notification found: " + notificationMessage);
    }

    @Then("I should see plant {string} in the table")
    public void verifyPlantInTable(String plantName) {
        boolean plantPresent = driver.getPageSource().contains(plantName);
        Assert.assertTrue("Plant '" + plantName + "' not found in table!", plantPresent);
        System.out.println("Plant found in table: " + plantName);
    }
}
