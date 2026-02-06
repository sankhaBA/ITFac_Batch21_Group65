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
      | name      | NewCat2    |
      | parent    |            |
    Then the response status code should be 201
    And the response body should contain "name" with value "NewCat2"

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
    Given a category exists with name "CatEdit2"
    When I send a PUT request to update the category with name "CatUpd2"
    Then the response status code should be 200
    And the response body should contain "name" with value "CatUpd2"

  Scenario: Verify PUT update category - Not Found
    Given I have a valid authentication token for "admin"
    When I send a PUT request to update a non-existent category with ID 999999
    Then the response status code should be 404

  Scenario: Verify PUT update category parent (Move Category)
    Given I have a valid authentication token for "admin"
    And a parent category exists with name "ParentCat"
    And a category exists with name "ChildCat"
    When I send a PUT request to link the category to the parent
    Then the response status code should be 200
    And the response body should contain "parent" with value from the parent category

  @API @Bug @SwaggerMismatch
  Scenario: Verify PUT request compliance with Swagger Schema (parentId)
    Given I have a valid authentication token for "admin"
    And a parent category exists with name "SwgrParnt"
    And a category exists with name "SwgrChld"
    When I send a PUT request to link the category using strictly "parentId"
    Then the response body should contain "parent" with value from the parent category

  Scenario: Verify DELETE category success
    Given a category exists with name "CatDel2"
    When I send a DELETE request for that category
    Then the response status code should be 204

  Scenario: Verify RBAC - User cannot Delete Category
    Given I have a valid authentication token for "user"
    And a category exists with name "UserDel2" (created by admin)
    When I send a DELETE request for that category
    Then the response status code should be 403