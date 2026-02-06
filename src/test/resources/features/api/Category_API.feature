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

  Scenario: Verify DELETE category success
    Given a category exists with name "CatDel2"
    When I send a DELETE request for that category
    Then the response status code should be 204

  Scenario: Verify RBAC - User cannot Delete Category
    Given I have a valid authentication token for "user"
    And a category exists with name "UserDel2" (created by admin)
    When I send a DELETE request for that category
    Then the response status code should be 403

  # ========== Additional GET Endpoint Tests ==========

  Scenario: Verify GET categories filtered by name
    Given a category exists with name "Floral"
    When I send a GET request to "/api/categories?name=Floral"
    Then the response status code should be 200
    And the response should contain category with name containing "Floral"

  Scenario: Verify GET categories filtered by parentId
    Given I have a valid authentication token for "admin"
    And a parent category exists with name "MainCat"
    And a category exists with name "SubCat1"
    And the category is linked to the parent
    When I send a GET request to categories filtered by parent ID
    Then the response status code should be 200
    And the response should contain only categories with the specified parent

  Scenario: Verify GET categories filtered by both name and parentId
    Given I have a valid authentication token for "admin"
    And a parent category exists with name "ParCat2"
    And a category exists with name "SubTest"
    And the category is linked to the parent
    When I send a GET request with name "Test" and parent ID filter
    Then the response status code should be 200
    And the response should contain category with name containing "Test" and specified parent

  Scenario: Verify GET categories with non-existent name returns empty array
    When I send a GET request to "/api/categories?name=NonExistentCategory12345"
    Then the response status code should be 200
    And the response should be an empty array

  Scenario: Verify GET categories with non-existent parentId returns empty array
    When I send a GET request to "/api/categories?parentId=999999"
    Then the response status code should be 200
    And the response should be an empty array

  Scenario: Verify GET single category by ID
    Given a category exists with name "GetById"
    When I send a GET request for that specific category by ID
    Then the response status code should be 200
    And the response body should contain "name" with value "GetById"

  Scenario: Verify GET category by non-existent ID returns 404
    When I send a GET request to "/api/categories/999999"
    Then the response status code should be 404

  # ========== Additional POST Endpoint Tests ==========

  Scenario: Verify RBAC - User cannot create category
    Given I have a valid authentication token for "user"
    When I send a POST request to "/api/categories" with the following body:
      | name   | UserCat |
    Then the response status code should be 403

  Scenario Outline: Verify POST create category with special characters
    When I send a POST request to "/api/categories" with the following body:
      | name   | <Name> |
    Then the response status code should be <Status>

    Examples:
      | Name       | Status |
      | Cat-123    | 400    |
      | Cat@Test   | 400    |
      | Cat_Item   | 400    |

  # ========== Additional PUT Endpoint Tests ==========

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

  Scenario: Verify DELETE non-existent category returns 404
    When I send a DELETE request to "/api/categories/999999"
    Then the response status code should be 404