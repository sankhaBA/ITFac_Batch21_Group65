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
    When I enter "Rose Plant 123" into the Plant Name field
    And I select a category
    And I enter "150" into the Price field
    And I enter "20" into the Quantity field
    And I click the Save button
    Then I should be navigated to the "Plants" page
    And I should see notification "Plant added successfully"
    And I should see plant "Rose Plant 123" in the table
