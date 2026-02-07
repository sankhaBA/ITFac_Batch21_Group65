@API @Sales
Feature: Sales Management API Automation

  Background:
    Given the application is running

  @API-SALES-GetHistory-01
  Scenario Outline: Verify <role> can view sales history
    Given I am logged in as "<role>" via API
    When I send a GET request to sales API "/api/sales"
    Then the sales API response status code should be 200
    And the response should contain a list of sales

    Examples:
      | role  |
      | Admin |
      | User  |

  @API-SALES-PostCreate-02
  Scenario: Admin successfully creates a sale via API
    Given I am logged in as "Admin" via API
    When I request to sell plant ID "1" with quantity "2"
    Then the sales API response status code should be 201

  # SRS Requirement: User CANNOT create a sale (Should be 403)
  @API-SALES-PostRBAC-03
  Scenario: User cannot create a sale
    Given I am logged in as "User" via API
    When I request to sell plant ID "1" with quantity "1"
    # This test WILL FAIL if the app has a bug (returns 201). This is GOOD.
    Then the sales API response status code should be 403

  @API-SALES-PostInvalid-04
  Scenario Outline: Fail sale creation with invalid quantity
    Given I am logged in as "Admin" via API
    When I request to sell plant ID "1" with quantity "<qty>"
    Then the sales API response status code should be 400
    And the sales API error message should be "Quantity must be greater than 0"

    Examples:
      | qty |
      | 0   |
      | -5  |

  @API-SALES-PostStock-05
  Scenario: Fail on Insufficient Stock
    Given I am logged in as "Admin" via API
    When I request to sell plant ID "1" with quantity "1000"
    Then the sales API response status code should be 400

  @API-SALES-DeleteSuccess-06
  Scenario: Admin Delete Sale (Success)
    Given I am logged in as "Admin" via API
    When I request to sell plant ID "1" with quantity "1"
    And I note the sale ID
    When I send a DELETE request to that sale ID
    # SRS doesn't specify 200 vs 204, but 204 is standard success.
    # If this fails with 200, report it as a minor consistency bug.
    Then the sales API response status code should be 204

  # SRS Requirement: User CANNOT delete a sale (Should be 403)
  @API-SALES-DeleteRBAC-07
  Scenario: User Delete Sale (Fail)
    Given I am logged in as "Admin" via API
    When I request to sell plant ID "1" with quantity "1"
    And I note the sale ID
    Given I am logged in as "User" via API
    When I send a DELETE request to that sale ID
    # This test WILL FAIL if the app allows it (returns 204/200). This is GOOD.
    Then the sales API response status code should be 403