@API @Category
Feature: Category Management API
  As a system, I want to expose endpoints for Category management
  So that the frontend can interact with the database

  Background:
    Given I have a valid authentication token for "admin"

  @API-CAT-GetAll-01
  Scenario: Verify GET all categories returns 200 OK
    When I send a GET request to "/api/categories"
    Then the response status code should be 200
    And the response should contain a list of categories

  @API-CAT-PostCreate-02
  Scenario: Verify POST create category success
    When I send a POST request to "/api/categories" with the following body:
      | name      | NewCat2    |
      | parent    |            |
    Then the response status code should be 201
    And the response body should contain "name" with value "NewCat2"

  @API-CAT-PostValidate-03
  Scenario Outline: Verify POST create category validations (Negative)
    When I send a POST request to "/api/categories" with the following body:
      | name   | <Name> |
    Then the response status code should be 400

    Examples:
      | Name          |
      | AB            |
      | VeryLongName1 |
      |               |

  @API-CAT-PutUpdate-04
  Scenario: Verify PUT update category success
    Given a category exists with name "CatEdit2"
    When I send a PUT request to update the category with name "CatUpd2"
    Then the response status code should be 200
    And the response body should contain "name" with value "CatUpd2"

  @API-CAT-PutNotFound-05
  Scenario: Verify PUT update category - Not Found
    Given I have a valid authentication token for "admin"
    When I send a PUT request to update a non-existent category with ID 999999
    Then the response status code should be 404

  @API-CAT-PutParent-06
  Scenario: Verify PUT update category parent (Move Category)
    Given I have a valid authentication token for "admin"
    And a parent category exists with name "ParentCat"
    And a category exists with name "ChildCat"
    When I send a PUT request to link the category to the parent
    Then the response status code should be 200
    And the response body should contain "parent" with value from the parent category

  @API-CAT-DeleteSuccess-07
  Scenario: Verify DELETE category success
    Given a category exists with name "CatDel2"
    When I send a DELETE request for that category
    Then the response status code should be 204

  @API-CAT-DeleteRBAC-08
  Scenario: Verify RBAC - User cannot Delete Category
    Given I have a valid authentication token for "user"
    And a category exists with name "UserDel2" (created by admin)
    When I send a DELETE request for that category
    Then the response status code should be 403

  # ========== Additional GET Endpoint Tests ==========

  @API-CAT-GetByName-09
  Scenario: Verify GET categories filtered by name
    When I send a GET request to "/api/categories?name=Flowers"
    Then the response status code should be 200
    And the response should contain category with name containing "Flowers"

  @API-CAT-GetByParent-10
  Scenario: Verify GET categories filtered by parentId
    Given I have a valid authentication token for "admin"
    When I send a GET request to categories filtered by parent named "Flowers"
    Then the response status code should be 200
    And the response should contain only categories with the specified parent

  @API-CAT-GetByBoth-11
  Scenario: Verify GET categories filtered by both name and parentId
    Given I have a valid authentication token for "admin"
    When I send a GET request with name "Roses" and parent named "Flowers"
    Then the response status code should be 200
    And the response should contain category with name containing "Roses" and specified parent

  @API-CAT-GetEmpty-12
  Scenario: Verify GET categories with non-existent name returns empty array
    When I send a GET request to "/api/categories?name=NonExistentCategory12345"
    Then the response status code should be 200
    And the response should be an empty array

  @API-CAT-GetEmptyParent-13
  Scenario: Verify GET categories with non-existent parentId returns empty array
    When I send a GET request to "/api/categories?parentId=999999"
    Then the response status code should be 200
    And the response should be an empty array

  @API-CAT-GetById-14
  Scenario: Verify GET single category by ID
    Given I have a valid authentication token for "admin"
    When I send a GET request for seeded category "Flowers" by ID
    Then the response status code should be 200
    And the response body should contain "name" with value "Flowers"

  @API-CAT-Get404-15
  Scenario: Verify GET category by non-existent ID returns 404
    When I send a GET request to "/api/categories/999999"
    Then the response status code should be 404

  # ========== Additional POST Endpoint Tests ==========

  @API-CAT-PostRBAC-16
  Scenario: Verify RBAC - User cannot create category
    Given I have a valid authentication token for "user"
    When I send a POST request to "/api/categories" with the following body:
      | name   | UserCat |
    Then the response status code should be 403

  # ========== Additional PUT Endpoint Tests ==========

  @API-CAT-PutInvalid-17
  Scenario Outline: Verify PUT update category with invalid names
    Given a category exists with name "TestCat3"
    When I send a PUT request to update the category with name "<Name>"
    Then the response status code should be 400

    Examples:
      | Name             |
      | AB               |
      | TooLongName123   |
      |                  |

  # ========== Additional DELETE Endpoint Tests ==========

  @API-CAT-Delete404-18
  Scenario: Verify DELETE non-existent category returns 404
    When I send a DELETE request to "/api/categories/999999"
    Then the response status code should be 404