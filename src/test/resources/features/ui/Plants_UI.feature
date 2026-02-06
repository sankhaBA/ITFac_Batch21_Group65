@UI
Feature: Plants UI Module
  I want to verify the Plants page navigation and form validation

  Background:
    Given I open the application login page
    And I login as "admin" with password "admin123"
    And I navigate to the Plants page

  Scenario: Verify Navigating to the Add Plant Page
    When I click the Add Plant button
    Then I should be navigated to the "Add Plant" page

  Scenario: Verify empty validation error when saving a plant without any data entering
    Given I am on the Add Plant page
    When I click the Save button without entering any data
    Then I should see validation error "Plant name is required" in red
    And I should see validation error "Plant name must be between 3 and 25 characters" in red
    And I should see validation error "Category is required" in red
    And I should see validation error "Price is required" in red
    And I should see validation error "Quantity is required" in red

  Scenario: Verify 3-25 characters validation in Plant Name input field - Less than 3 characters
    Given I am on the Add Plant page
    When I enter "tc" into the Plant Name field
    And I select a category
    And I enter "100" into the Price field
    And I enter "10" into the Quantity field
    And I click the Save button
    Then I should see validation error "Plant name must be between 3 and 25 characters" in red

  Scenario: Verify 3-25 characters validation in Plant Name input field - More than 25 characters
    Given I am on the Add Plant page
    When I enter "test case plant name 0000001" into the Plant Name field
    And I select a category
    And I enter "100" into the Price field
    And I enter "10" into the Quantity field
    And I click the Save button
    Then I should see validation error "Plant name must be between 3 and 25 characters" in red

  Scenario: Verify 3-25 characters validation in Plant Name input field - Exactly 3 characters
    Given I am on the Add Plant page
    When I enter "tsc" into the Plant Name field
    And I select a category
    And I enter "100" into the Price field
    And I enter "10" into the Quantity field
    And I click the Save button
    Then I should not see validation error "Plant name must be between 3 and 25 characters"

  Scenario: Verify 3-25 characters validation in Plant Name input field - Exactly 25 characters
    Given I am on the Add Plant page
    When I enter "test case plant name 0001" into the Plant Name field
    And I select a category
    And I enter "100" into the Price field
    And I enter "10" into the Quantity field
    And I click the Save button
    Then I should not see validation error "Plant name must be between 3 and 25 characters"

  Scenario: Verify 3-25 characters validation in Plant Name input field - Between 3 and 25 characters
    Given I am on the Add Plant page
    When I enter "test plant 001" into the Plant Name field
    And I select a category
    And I enter "100" into the Price field
    And I enter "10" into the Quantity field
    And I click the Save button
    Then I should not see validation error "Plant name must be between 3 and 25 characters"

  Scenario: Verify cancel plant action in Add Plant page to navigate to the Plants page
    Given I am on the Add Plant page
    When I click the Cancel button
    Then I should be navigated to the "Plants" page

  Scenario: Verify Save action in Add Plant page with every data
    Given I am on the Add Plant page
    When I enter a unique plant name into the Plant Name field
    And I select a category
    And I enter "150" into the Price field
    And I enter "20" into the Quantity field
    And I click the Save button
    Then I should be navigated to the "Plants" page
    And I should see alert "Plant added successfully"
    And I should see the newly saved plant in the table

  Scenario: Verify delete plant action in the table
    When I click the Delete button for the first plant record
    Then the plant record should be removed from the table

  Scenario: Verify edit button action in the table to navigate into its "Edit Plant" page
    When I click the Edit button for the first plant record
    Then I should be navigated to the "Edit Plant" page
    And the plant details should be loaded in the form

  Scenario: Verify cancel plant action in "Edit Plant" page to navigate to the "Plants" page
    Given I am on the Edit Plant page
    When I click the Cancel button
    Then I should be navigated to the "Plants" page

  Scenario: Verify 3-25 characters validation in Plant Name input field in "Edit Plant" page
    Given I am on the Edit Plant page
    When I clear the Plant Name field
    And I enter "tc" into the Plant Name field
    And I click the Save button
    Then I should see validation error "Plant name must be between 3 and 25 characters" in red

  Scenario: Verify Save action in "Edit Plant" page with empty data
    Given I am on the Edit Plant page
    When I clear all plant input fields
    And I click the Save button
    Then I should see validation error "Plant name is required" in red
    And I should see validation error "Plant name must be between 3 and 25 characters" in red
    And I should see validation error "Category is required" in red
    And I should see validation error "Price is required" in red
    And I should see validation error "Quantity is required" in red

  Scenario: Verify "Save" action in "Edit Plant" page with every data
    Given I am on the Edit Plant page
    When I enter a unique plant name into the Plant Name field
    And I select a category
    And I enter "200" into the Price field
    And I enter "15" into the Quantity field
    And I click the Save button
    Then I should be navigated to the "Plants" page
    And I should see alert "Plant updated successfully"
    And I should see the newly saved plant in the table

  Scenario: Verify showing Low stock badge in the Stock column of the table
    Given I am on the Edit Plant page
    When I enter "3" into the Quantity field
    And I click the Save button
    Then I should see low stock badge for the updated plant

  Scenario: Verify "Search plant" search action with fake data
    When I search for plant "NoSuchPlant123"
    Then I should see "No plants found" in the table

  Scenario: Verify "Search plant" search action with real data
    When I search for the first plant record
    Then I should see the searched plant in the table

  Scenario: Verify category filtering
    When I filter plants by a category
    Then I should see plants filtered by the selected category

  Scenario: Verify sorting in the plant table
    When I click the "Name" column header
    Then the plant table should be sorted by "Name"

  Scenario: Verify price validation when price is 0 or negative
    Given I am on the Edit Plant page
    When I enter "Valid Plant" into the Plant Name field
    And I select a category
    And I enter "0" into the Price field
    And I enter "5" into the Quantity field
    And I click the Save button
    Then I should see validation error "Price must be greater than 0" in red

  Scenario: Verify quantity validation when quantity is negative
    Given I am on the Edit Plant page
    When I enter "Valid Plant" into the Plant Name field
    And I select a category
    And I enter "100" into the Price field
    And I enter "-1" into the Quantity field
    And I click the Save button
    Then I should see validation error "Quantity cannot be negative" in red
