@API
Feature: Authentication API Module
  I want to verify the backend security and token generation

  Background:
    Given the backend API is running at "http://localhost:8080"

  Scenario Outline: Verify Login Success for Valid Roles
    When I send a POST request to "/api/auth/login" with username "<username>" and password "<password>"
    Then the response status code should be 200
    And the response should contain a valid "token"

    Examples:
      | username | password  | role  |
      | admin    | admin123  | Admin |
      | testuser | test123   | User  |

  Scenario Outline: Verify Login Failures (Invalid Credentials & Validation)
    When I send a POST request to "/api/auth/login" with username "<username>" and password "<password>"
    Then the response status code should be <statusCode>
    And the response error message should be "<errorMsg>"

    Examples:
      | username | password      | statusCode | errorMsg     | Test Case ID      |
      | admin    | wrong         | 401        | UNAUTHORIZED | API-AUTH-Login-03 |
      |          | admin123      | 400        | BAD REQUEST  | API-AUTH-Login-04 |
      | admin    |               | 400        | BAD REQUEST  | API-AUTH-Login-05 |
      |          |               | 400        | BAD REQUEST  | API-AUTH-Login-06 |

  Scenario: Verify SQL Injection Protection
    When I send a POST request to "/api/auth/login" with username "admin" and password "' OR '1'='1"
    Then the response status code should be 401
    And the response error message should be "UNAUTHORIZED"