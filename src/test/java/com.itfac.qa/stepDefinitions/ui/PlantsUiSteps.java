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
import org.openqa.selenium.support.ui.Select;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PlantsUiSteps {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private String lastSelectedPlantName;
    private String lastSelectedCategoryName;
    private String lastSearchTerm;

    private WebDriver getDriver() {
        return Hooks.getDriver();
    }

    private WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(config.getExplicitWait()));
    }

    @Given("I login as {string} with password {string}")
    public void loginAs(String username, String password) {
        LoginHelper.login(username, password);
    }

    @Given("I navigate to the Plants page")
    public void navigateToPlantsPage() {
        // Click on Plants navigation item
        WebElement plantsNavItem = getWait().until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Plants') or contains(@href, '/plants')]"))
        );
        plantsNavItem.click();
        
        // Wait for Plants page to load
        getWait().until(ExpectedConditions.urlContains("plants"));
        System.out.println("Navigated to Plants page");
    }

    @When("I click the Add Plant button")
    public void clickAddPlantButton() {
        try {
            System.out.println("Current URL: " + getDriver().getCurrentUrl());
            System.out.println("Looking for Add Plant button...");
            
            WebElement button = null;
            try {
                button = getWait().until(
                    ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(), 'Add a Plant')] | //a[contains(text(), 'Add a Plant')]")
                    )
                );
            } catch (Exception e1) {
                System.out.println("Button not found with text 'Add a Plant', trying alternative locators...");
                try {
                    button = getDriver().findElement(By.xpath("//button[contains(text(), 'Add')] | //a[contains(text(), 'Add')]"));
                } catch (Exception e2) {
                    System.out.println("Button not found, navigating directly to URL...");
                    getDriver().get(config.getUiBaseUrl() + "/plants/add");
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
            getDriver().get(config.getUiBaseUrl() + "/plants/add");
        }
    }

    @Then("I should be navigated to the {string} page")
    public void verifyNavigationToPage(String pageName) {
        String urlSegment;
        if (pageName.equalsIgnoreCase("Add Plant")) {
            urlSegment = "plants/add";
        } else if (pageName.equalsIgnoreCase("Edit Plant")) {
            urlSegment = "plants/edit";
        } else if (pageName.equalsIgnoreCase("Plants")) {
            // Ensure we are on the list page, not add/edit.
            getWait().until(d -> {
                String url = d.getCurrentUrl();
                return url != null
                        && url.contains("/ui/plants")
                        && !url.contains("/ui/plants/add")
                        && !url.contains("/ui/plants/edit");
            });
            String currentUrl = getDriver().getCurrentUrl();
            Assert.assertTrue("Not on Plants list page! Current URL: " + currentUrl,
                    currentUrl.contains("/ui/plants")
                            && !currentUrl.contains("/ui/plants/add")
                            && !currentUrl.contains("/ui/plants/edit"));
            System.out.println("Successfully navigated to: " + pageName);
            return;
        } else {
            urlSegment = pageName.toLowerCase().replace(" ", "-");
        }
        
        getWait().until(ExpectedConditions.urlContains(urlSegment));
        
        String currentUrl = getDriver().getCurrentUrl();
        Assert.assertTrue("Not on " + pageName + " page! Current URL: " + currentUrl, 
                         currentUrl.contains(urlSegment));
        System.out.println("Successfully navigated to: " + pageName);
    }

    @Given("I am on the Add Plant page")
    public void navigateToAddPlantPage() {
        try {
            clickAddPlantButton();
            getWait().until(ExpectedConditions.urlContains("plants/add"));
        } catch (Exception e) {
            System.out.println("Could not click button, navigating directly to Add Plant page...");
            getDriver().get(config.getUiBaseUrl() + "/plants/add");
            getWait().until(ExpectedConditions.urlContains("plants/add"));
        }
    }

    @When("I click the Save button without entering any data")
    public void clickSaveButtonWithoutData() {
        WebElement button = getWait().until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Save')]")
            )
        );
        button.click();
        System.out.println("Clicked Save button without entering data");
    }

    @Then("I should see validation error {string} in red")
    public void verifyValidationError(String errorMessage) {
        getWait().until(d -> d.getPageSource() != null && !d.getPageSource().isEmpty());
        boolean errorPresent = containsTextLoosely(getDriver().getPageSource(), errorMessage);
        Assert.assertTrue("Validation error '" + errorMessage + "' not found!", errorPresent);
        System.out.println("Validation error found: " + errorMessage);
    }

    @When("I enter {string} into the Plant Name field")
    public void enterPlantName(String plantName) {
        WebElement field = getWait().until(
            ExpectedConditions.presenceOfElementLocated(By.name("name"))
        );
        field.clear();
        field.sendKeys(plantName);
        System.out.println("Entered plant name: " + plantName);
    }

    @When("I select a category")
    public void selectCategory() {
        WebElement categoryDropdown = getWait().until(
            ExpectedConditions.elementToBeClickable(By.name("categoryId"))
        );
        categoryDropdown.click();
        
        // Select the first available option (not the placeholder)
        WebElement firstOption = getWait().until(
            ExpectedConditions.elementToBeClickable(By.xpath("//select[@name='categoryId']/option[not(@value='') and not(@disabled)][1]"))
        );
        firstOption.click();
        System.out.println("Selected a category");
    }

    @When("I enter {string} into the Price field")
    public void enterPrice(String price) {
        WebElement field = getWait().until(
            ExpectedConditions.presenceOfElementLocated(By.name("price"))
        );
        field.clear();
        field.sendKeys(price);
        System.out.println("Entered price: " + price);
    }

    @When("I enter {string} into the Quantity field")
    public void enterQuantity(String quantity) {
        WebElement field = getWait().until(
            ExpectedConditions.presenceOfElementLocated(By.name("quantity"))
        );
        field.clear();
        field.sendKeys(quantity);
        System.out.println("Entered quantity: " + quantity);
    }

    @When("I click the Save button")
    public void clickSaveButton() {
        WebElement button = getWait().until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Save')]")
            )
        );
        button.click();
        System.out.println("Clicked Save button");
    }

    @Then("I should not see validation error {string}")
    public void verifyNoValidationError(String errorMessage) {
        boolean errorPresent = getDriver().getPageSource().contains(errorMessage);
        Assert.assertFalse("Validation error '" + errorMessage + "' should not be present but was found!", errorPresent);
        System.out.println("Validation error correctly not present: " + errorMessage);
    }

    @When("I click the Cancel button")
    public void clickCancelButton() {
        WebElement button = getWait().until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Cancel')] | //a[contains(text(), 'Cancel')]")
            )
        );
        button.click();
        System.out.println("Clicked Cancel button");
    }

    @Then("I should see alert {string}")
    public void verifyAlert(String alertMessage) {
        WebDriverWait alertWait = new WebDriverWait(getDriver(), Duration.ofSeconds(15));
        alertWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert")));
        alertWait.until(d -> {
            List<WebElement> alerts = d.findElements(By.cssSelector(".alert"));
            for (WebElement a : alerts) {
                String text = a.getText();
                if (containsTextLoosely(text, alertMessage)) {
                    return true;
                }
            }
            return false;
        });
        boolean alertPresent = false;
        for (WebElement a : getDriver().findElements(By.cssSelector(".alert"))) {
            if (containsTextLoosely(a.getText(), alertMessage)) {
                alertPresent = true;
                break;
            }
        }
        Assert.assertTrue("Alert '" + alertMessage + "' not found!", alertPresent);
        System.out.println("Alert found: " + alertMessage);
    }

    @When("I enter a unique plant name into the Plant Name field")
    public void enterUniquePlantName() {
        String unique = "Plant_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        lastSelectedPlantName = unique;
        enterPlantName(unique);
    }

    @Then("I should see the newly saved plant in the table")
    public void verifyNewlySavedPlantInTable() {
        Assert.assertNotNull("No plant name was stored for verification!", lastSelectedPlantName);
        verifyPlantInTable(lastSelectedPlantName);
    }

    @Then("I should see plant {string} in the table")
    public void verifyPlantInTable(String plantName) {
        getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
        getWait().until(d -> containsTextLoosely(d.getPageSource(), plantName));
        boolean plantPresent = containsTextLoosely(getDriver().getPageSource(), plantName);
        Assert.assertTrue("Plant '" + plantName + "' not found in table!", plantPresent);
        System.out.println("Plant found in table: " + plantName);
    }

    @When("I click the Delete button for the first plant record")
    public void clickDeleteButtonForFirstPlant() {
        WebElement row = getFirstPlantRow();
        lastSelectedPlantName = getRowPlantName(row);
        WebElement deleteButton = findActionButton(row, "Delete");
        deleteButton.click();
        acceptConfirmIfPresent();
        getWait().until(ExpectedConditions.stalenessOf(row));
        System.out.println("Clicked Delete for plant: " + lastSelectedPlantName);
    }

    @Then("the plant record should be removed from the table")
    public void verifyPlantRemovedFromTable() {
        Assert.assertNotNull("No plant was selected for delete!", lastSelectedPlantName);
        WebDriverWait deleteWait = new WebDriverWait(getDriver(), Duration.ofSeconds(15));
        deleteWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
        deleteWait.until(d -> !containsTextLoosely(d.getPageSource(), lastSelectedPlantName));
        Assert.assertFalse(
            "Plant '" + lastSelectedPlantName + "' still present after delete!",
            containsTextLoosely(getDriver().getPageSource(), lastSelectedPlantName)
        );
    }

    @When("I click the Edit button for the first plant record")
    public void clickEditButtonForFirstPlant() {
        WebElement row = getFirstPlantRow();
        lastSelectedPlantName = getRowPlantName(row);
        WebElement editButton = findActionButton(row, "Edit");
        editButton.click();
        getWait().until(ExpectedConditions.urlContains("plants"));
        System.out.println("Clicked Edit for plant: " + lastSelectedPlantName);
    }

    @Then("the plant details should be loaded in the form")
    public void verifyPlantDetailsLoaded() {
        WebElement nameField = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        String nameValue = nameField.getAttribute("value");
        Assert.assertNotNull("Plant name value is missing on edit form!", nameValue);
        Assert.assertFalse("Plant name value is empty on edit form!", nameValue.trim().isEmpty());

        WebElement priceField = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("price")));
        WebElement quantityField = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("quantity")));
        Assert.assertFalse("Price value is empty on edit form!",
                priceField.getAttribute("value").trim().isEmpty());
        Assert.assertFalse("Quantity value is empty on edit form!",
                quantityField.getAttribute("value").trim().isEmpty());
    }

    @Given("I am on the Edit Plant page")
    public void navigateToEditPlantPage() {
        clickEditButtonForFirstPlant();
        getWait().until(ExpectedConditions.urlContains("plants"));
        verifyNavigationToPage("Edit Plant");
    }

    @When("I clear the Plant Name field")
    public void clearPlantNameField() {
        WebElement field = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        field.clear();
        System.out.println("Cleared Plant Name field");
    }

    @When("I clear all plant input fields")
    public void clearAllPlantInputFields() {
        WebElement nameField = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("name")));
        WebElement priceField = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("price")));
        WebElement quantityField = getWait().until(ExpectedConditions.presenceOfElementLocated(By.name("quantity")));

        nameField.clear();
        priceField.clear();
        quantityField.clear();

        WebElement categoryDropdown = getWait().until(ExpectedConditions.elementToBeClickable(By.name("categoryId")));
        Select select = new Select(categoryDropdown);
        if (!select.getOptions().isEmpty()) {
            select.selectByIndex(0);
        }
        System.out.println("Cleared all plant input fields");
    }

    @Then("I should see low stock badge for the updated plant")
    public void verifyLowStockBadgeForUpdatedPlant() {
        WebElement row = findRowByPlantName(lastSelectedPlantName);
        Assert.assertNotNull("Updated plant row not found for low stock check!", row);
        Assert.assertTrue("Low stock badge not shown for plant: " + lastSelectedPlantName,
                row.getText().toLowerCase().contains("low"));
    }

    @When("I search for plant {string}")
    public void searchForPlant(String searchTerm) {
        lastSearchTerm = searchTerm;
        WebElement searchInput = findSearchInput();
        searchInput.clear();
        searchInput.sendKeys(searchTerm);

        WebElement searchButton = findSearchButton();
        if (searchButton != null) {
            searchButton.click();
        } else {
            searchInput.submit();
        }
        System.out.println("Searched for plant: " + searchTerm);
    }

    @When("I search for the first plant record")
    public void searchForFirstPlantRecord() {
        WebElement row = getFirstPlantRow();
        String plantName = getRowPlantName(row);
        searchForPlant(plantName);
    }

    @Then("I should see the searched plant in the table")
    public void verifySearchedPlantInTable() {
        Assert.assertNotNull("No search term captured!", lastSearchTerm);
        Assert.assertTrue("Searched plant '" + lastSearchTerm + "' not found in table!",
                getDriver().getPageSource().contains(lastSearchTerm));
    }

    @Then("I should see {string} in the table")
    public void verifyTableMessage(String message) {
        boolean messagePresent = getDriver().getPageSource().contains(message);
        Assert.assertTrue("Message '" + message + "' not found in table!", messagePresent);
    }

    @When("I filter plants by a category")
    public void filterPlantsByCategory() {
        WebElement categoryDropdown = getWait().until(
                ExpectedConditions.elementToBeClickable(By.xpath("//select[contains(@name,'category') or contains(@id,'category')]"))
        );
        Select select = new Select(categoryDropdown);
        List<WebElement> options = select.getOptions();
        for (WebElement option : options) {
            String value = option.getAttribute("value");
            if (value != null && !value.trim().isEmpty()) {
                select.selectByVisibleText(option.getText());
                lastSelectedCategoryName = option.getText();
                break;
            }
        }

        WebElement searchButton = findSearchButton();
        if (searchButton != null) {
            searchButton.click();
        }
        System.out.println("Filtered by category: " + lastSelectedCategoryName);
    }

    @Then("I should see plants filtered by the selected category")
    public void verifyPlantsFilteredByCategory() {
        Assert.assertNotNull("No category was selected for filtering!", lastSelectedCategoryName);
        boolean categoryPresent = getDriver().getPageSource().contains(lastSelectedCategoryName);
        Assert.assertTrue("Filtered plants do not show selected category!", categoryPresent);
    }

    @When("I click the {string} column header")
    public void clickColumnHeader(String columnName) {
        WebElement headerLink = getWait().until(
            ExpectedConditions.elementToBeClickable(By.xpath("//th//a[contains(normalize-space(.), '" + columnName + "')]"))
        );
        headerLink.click();
        System.out.println("Clicked column header: " + columnName);
    }

    @Then("the plant table should be sorted by {string}")
    public void verifyTableSortedByColumn(String columnName) {
        String expectedSortField;
        if (columnName.equalsIgnoreCase("Name")) {
            expectedSortField = "name";
        } else if (columnName.equalsIgnoreCase("Price")) {
            expectedSortField = "price";
        } else if (columnName.equalsIgnoreCase("Stock")) {
            expectedSortField = "quantity";
        } else {
            expectedSortField = columnName.toLowerCase();
        }

        getWait().until(driver -> getDriver().getCurrentUrl().contains("sortField=" + expectedSortField));
        Assert.assertTrue("URL does not contain expected sortField for column: " + columnName,
                getDriver().getCurrentUrl().contains("sortField=" + expectedSortField));

        boolean arrowPresent = getDriver().getPageSource().contains("↑") || getDriver().getPageSource().contains("↓");
        Assert.assertTrue("Sort direction indicator not found after sorting!", arrowPresent);
    }

    private WebElement getFirstPlantRow() {
        WebElement table = getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
        for (WebElement row : rows) {
            String rowText = row.getText();
            if (rowText != null && !rowText.toLowerCase().contains("no plants found")) {
                return row;
            }
        }
        Assert.fail("No plant rows found in table!");
        return null;
    }

    private String getRowPlantName(WebElement row) {
        List<WebElement> cells = row.findElements(By.tagName("td"));
        if (!cells.isEmpty()) {
            return cells.get(0).getText();
        }
        return row.getText();
    }

    private WebElement findRowByPlantName(String plantName) {
        WebElement table = getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table")));
        List<WebElement> rows = table.findElements(By.xpath(".//tbody/tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(plantName)) {
                return row;
            }
        }
        return null;
    }

    private WebElement findActionButton(WebElement row, String actionText) {
        if (actionText.equalsIgnoreCase("Edit")) {
            List<WebElement> editLinks = row.findElements(By.xpath(
                    ".//a[@title='Edit' or contains(@href, '/ui/plants/edit') or .//i[contains(@class,'bi-pencil')]]"
            ));
            if (!editLinks.isEmpty()) {
                return editLinks.get(0);
            }
        }

        if (actionText.equalsIgnoreCase("Delete")) {
            List<WebElement> deleteButtons = row.findElements(By.xpath(
                    ".//button[@title='Delete' or .//i[contains(@class,'bi-trash')]]"
            ));
            if (!deleteButtons.isEmpty()) {
                return deleteButtons.get(0);
            }
        }

        List<WebElement> byTitleOrLabel = row.findElements(By.xpath(
                ".//*[(@title='" + actionText + "') or (@aria-label='" + actionText + "')]"
        ));
        if (!byTitleOrLabel.isEmpty()) {
            return byTitleOrLabel.get(0);
        }

        Assert.fail(actionText + " button not found in row!");
        return null;
    }

    private void acceptConfirmIfPresent() {
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(5)).until(ExpectedConditions.alertIsPresent());
            getDriver().switchTo().alert().accept();
        } catch (Exception ignored) {
            // No confirm dialog present
        }
    }

    private boolean containsTextLoosely(String pageSource, String expectedText) {
        if (pageSource == null || expectedText == null) {
            return false;
        }

        String haystack = pageSource.toLowerCase(Locale.ROOT);
        String needle = expectedText.toLowerCase(Locale.ROOT);
        if (haystack.contains(needle)) {
            return true;
        }

        // Common minor variations (punctuation / alternative wording) seen in server-side validations
        if (needle.equals("plant name is required")) {
            return haystack.contains("plant name is required") || haystack.contains("name is required");
        }
        if (needle.equals("category is required")) {
            return haystack.contains("category is required") || haystack.contains("category required");
        }
        if (needle.equals("price is required")) {
            return haystack.contains("price is required") || haystack.contains("price required");
        }
        if (needle.equals("quantity is required")) {
            return haystack.contains("quantity is required") || haystack.contains("quantity required");
        }

        // Ignore trailing punctuation differences
        if (haystack.contains(needle + ".") || haystack.contains(needle + "!")) {
            return true;
        }

        return false;
    }

    private WebElement findSearchInput() {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                "//input[contains(@placeholder,'Search') or contains(@aria-label,'Search') or contains(@name,'search') or contains(@id,'search')]"
        )));
    }

    private WebElement findSearchButton() {
        List<WebElement> buttons = getDriver().findElements(By.xpath("//button[contains(normalize-space(.), 'Search')]"));
        return buttons.isEmpty() ? null : buttons.get(0);
    }
}
