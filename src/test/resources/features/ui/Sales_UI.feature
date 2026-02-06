@UI
Feature: Sales Management UI Automation

  # Covers: FrontendUI-SALES-Nav-01, Vis-01, Vis-02
  Scenario Outline: Verify UI elements based on role
    Given I navigate to the login page
    And I login as "<role>" via UI
    When I click the "Sales" link in the navigation
    Then I should be on the Sales List page
    And the "Sell Plant" button should be "<visibility>"
    And the "Delete" icon should be "<visibility>"

    Examples:
      | role  | visibility |
      | Admin | visible    |
      | User  | hidden     |

  # Covers: FrontendUI-SALES-Flow-03
  Scenario: Admin completes a successful sale via UI
    Given I navigate to the login page
    And I login as "Admin" via UI
    And I navigate to the "Sell Plant" page
    When I select "Fern" from the plant dropdown
    And I enter quantity "1"
    And I click the "Submit" button
    Then I should be redirected to the Sales List
    And the new sale should appear at the top of the list

  # Covers: FrontendUI-SALES-Form-01
  Scenario: Submit Empty Form Validation
    Given I navigate to the login page
    And I login as "Admin" via UI
    And I navigate to the "Sell Plant" page
    When I click the "Submit" button
    Then I should see a validation error "Plant is required" on the page

  # Covers: FrontendUI-SALES-Form-02
  Scenario: Submit Invalid Quantity Validation
    Given I navigate to the login page
    And I login as "Admin" via UI
    And I navigate to the "Sell Plant" page
    When I select "Fern" from the plant dropdown
    And I enter quantity "-5"
    And I click the "Submit" button
    Then I should see a validation error "Quantity must be greater than 0" on the page

  # Covers: FrontendUI-SALES-Del-01
  Scenario: Delete Confirmation
    Given I navigate to the login page
    And I login as "Admin" via UI
    When I click the "Sales" link in the navigation
    And I click the delete icon for the first sale
    Then I should see a confirmation popup
    And I accept the deletion