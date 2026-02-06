@UI
Feature: Sales Management UI Automation

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

  Scenario: Admin completes a successful sale via UI
    Given I navigate to the login page
    And I login as "Admin" via UI
    And I navigate to the "Sell Plant" page
    When I select "Fern" from the plant dropdown
    And I enter quantity "1"
    And I click the "Submit" button
    Then I should be redirected to the Sales List
    And the new sale should appear at the top of the list