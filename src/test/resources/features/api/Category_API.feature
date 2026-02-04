@API
Feature: Category Management API
  As a system, I want to expose endpoints for Category management
  So that the frontend can interact with the database

  Background:
    Given I have a valid authentication token for "admin"

  Scenario: Verify GET all categories returns 200 OK
    When I send a GET request to "/api/categories"
    Then the response status code should be 200
    And the response should contain a list of categories

  Scenario: Verify POST create category success
    When I send a POST request to "/api/categories" with the following body:
      | name      | validCat   |
      | parent    |            |
    Then the response status code should be 201
    And the response body should contain "name" with value "validCat"

  Scenario Outline: Verify POST create category validations (Negative)
    When I send a POST request to "/api/categories" with the following body:
      | name   | <Name> |
    Then the response status code should be 400

    Examples:
      | Name          |
      | AB            |
      | VeryLongName1 |
      |               |

  Scenario: Verify PUT update category success
    Given a category exists with name "ToUpdate"
    When I send a PUT request to update the category with name "UpdatedViaAPI"
    Then the response status code should be 200
    And the response body should contain "name" with value "UpdatedViaAPI"

  Scenario: Verify DELETE category success
    Given a category exists with name "ToDelete"
    When I send a DELETE request for that category
    Then the response status code should be 204

  Scenario: Verify RBAC - User cannot Delete Category
    Given I have a valid authentication token for "user"
    And a category exists with name "UserTryDelete" (created by admin)
    When I send a DELETE request for that category
    Then the response status code should be 403