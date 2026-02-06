@UI
Feature: Authentication UI Module

  Scenario Outline: Verify Valid Login Redirects
    Given I open the application login page
    When I enter username "<username>"
    And I enter password "<password>"
    And I click the login button
    Then I should be redirected to the Dashboard

    Examples:
      | username | password | Role  |
      | admin    | admin123 | Admin |
      | testuser | test123  | User  |

  Scenario: Verify Invalid Login Error Message
    Given I open the application login page
    When I enter username "admin"
    And I enter password "wrong"
    And I click the login button
    Then I should see an error message saying "Invalid username or password"

  Scenario Outline: Verify Field Validation Messages
    Given I open the application login page
    When I enter username "<username>"
    And I enter password "<password>"
    And I click the login button
    Then I should see a validation error "<validationMessage>" in red

    Examples:
      | username | password | validationMessage    |
      |          | admin123 | Username is required |
      | admin    |          | Password is required |
      |          |          | Username is required |

  Scenario: Verify Logout Functionality
    Given I am logged in as "testuser" with password "test123"
    # FIXED: Changed to a unique step (removed quotes) to avoid duplicate error
    When I click the logout button
    Then I should be redirected to the Login page
    And I should see a success message