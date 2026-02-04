@UI
Feature: Authentication UI Module
  I want to verify the frontend login page behavior

  Background:
    Given I open the application login page

  Scenario Outline: Verify Valid Login Redirects
    When I enter username "<username>"
    And I enter password "<password>"
    And I click the login button
    Then I should be redirected to the Dashboard

    Examples:
      | username | password | Role  |
      | admin    | admin123 | Admin |
      | testuser | test123  | User  |

  Scenario: Verify Invalid Login Error Message
    When I enter username "admin"
    And I enter password "wrong"
    And I click the login button
    Then I should see an error message saying "Invalid username or password"

  Scenario Outline: Verify Field Validation Messages
    When I enter username "<username>"
    And I enter password "<password>"
    And I click the login button
    Then I should see a validation error "<validationMessage>" in red

    Examples:
      | username | password | validationMessage    | Note              |
      |          | admin123 | Username is required | Empty Username    |
      | admin    |          | Password is required | Empty Password    |
      |          |          | Username is required | Both Empty (Main) |

  Scenario: Verify Logout Functionality
    Given I am logged in as "testuser" with password "test123"
    When I click the "Logout" button
    Then I should be redirected to the Login page
    And I should see a success message