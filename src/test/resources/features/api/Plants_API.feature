@API @Plants
Feature: Plants API Module
  I want to verify the Plants API endpoints with proper authentication

  Background:
    Given the backend API is running at "http://localhost:8080"

  @API-PLANT-GetById-01
  Scenario: Verify Get Plant by ID – Success
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to get the first seeded plant by ID
    Then the response status code should be 200
    And the response should contain plant details with field "id"
    And the response should contain plant details with field "name"
    And the response should contain plant details with field "price"
    And the response should contain plant details with field "quantity"
    And the response should contain plant details with field "categoryId"

  @API-PLANT-GetByIdFail-02
  Scenario Outline: Verify Get Plant by ID Failures
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to "<endpoint>" with the stored token
    Then the response status code should be <statusCode>
    And the response should contain error field "status" with value <statusCode>

    Examples:
      | endpoint        | statusCode | Test Case ID             |
      | /api/plants/abc | 500        | API-PLANTS-GetByID-02    |

  @API-PLANT-GetUnauth-03
  Scenario: Verify Get Plant by ID Unauthorized
    When I send a GET request to "/api/plants/1" without authentication
    Then the response status code should be 401
    And the response should contain error field "status" with value 401
    And the response should contain error field "error" with value "UNAUTHORIZED"

  @API-PLANT-PutSuccess-04
  Scenario: Verify Update Plant Success
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a PUT request to update the first seeded plant with name "Updated Rose" price 150 and quantity 25
    Then the response status code should be 200
    And the response should contain plant details with field "id"
    And the response should contain plant details with field "name"
    And the response should contain plant details with field "price"
    And the response should contain plant details with field "quantity"
    And the response should contain nested field "category.id"
    And the response should contain nested field "category.name"

  @API-PLANT-PutFail-05
  Scenario Outline: Verify Update Plant Failures
    Given I authenticate with username "<username>" and password "<password>" and store the token
    When I send a PUT request to "<endpoint>" with the stored token and payload:
      """
      <payload>
      """
    Then the response status code should be <statusCode>
    And the response should contain error field "status" with value <statusCode>

    Examples:
      | username | password | endpoint        | statusCode | payload                                                                                                                                      | Test Case ID          |
      | admin    | admin123 | /api/plants/1000| 400        | {"id": 1000}                                                                                                                                 | API-PLANTS-Update-02  |
      | testuser | test123  | /api/plants/FIRST_SEEDED_PLANT_ID   | 403        | USE_FIRST_SEEDED_PLANT_DATA | API-PLANTS-Update-03  |
      | admin    | admin123 | /api/plants/FIRST_SEEDED_PLANT_ID   | 500        | USE_INVALID_CATEGORY_PAYLOAD                                          | API-PLANTS-Update-04  |

  @API-PLANT-DeleteSuccess-06
  Scenario: Verify Delete Plant by ID Success
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a DELETE request to delete the last seeded plant by ID
    Then the response status code should be 204

  @API-PLANT-DeleteFail-07
  Scenario Outline: Verify Delete Plant by ID Failures
    Given I authenticate with username "<username>" and password "<password>" and store the token
    When I send a DELETE request to "<endpoint>" with the stored token
    Then the response status code should be <statusCode>
    And the response should contain error field "status" with value <statusCode>

    Examples:
      | username | password | endpoint        | statusCode | Test Case ID          |
      | admin    | admin123 | /api/plants/abc | 400        | API-PLANTS-Delete-02  |
      | testuser | test123  | /api/plants/FIRST_SEEDED_PLANT_ID   | 403        | API-PLANTS-Delete-04  |
      | admin    | admin123 | /api/plants/999999 | 404        | API-PLANTS-Delete-05  |

  @API-PLANT-DeleteUnauth-08
  Scenario: Verify Delete Plant by ID Unauthorized
    When I send a DELETE request to delete first seeded plant without authentication
    Then the response status code should be 401
    And the response should contain error field "status" with value 401
    And the response should contain error field "error" with value "UNAUTHORIZED"

  @API-PLANT-GetByCat-09
  Scenario: Verify Get Plants by Category Success
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to "/api/plants/category/{categoryId}" with the stored token using an existing category id
    Then the response status code should be 200
    And the response should contain plant details with field "[0].id"
    And the response should contain plant details with field "[0].name"
    And the response should contain plant details with field "[0].price"
    And the response should contain plant details with field "[0].quantity"
    And the response should contain nested field "[0].category.id"
    And the response should contain nested field "[0].category.name"

  @API-PLANT-GetCatInvalid-10
  Scenario: Verify Get Plants by Category Invalid Category Format
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to "/api/plants/category/abc" with the stored token
    Then the response status code should be 500
    And the response should contain error field "status" with value 500
    And the response should contain error field "error" with value "INTERNAL_SERVER_ERROR"
    And the response should contain error field "message" containing "Failed to convert value"

  @API-PLANT-GetCatEmpty-11
  Scenario: Verify Get Plants by Category Not Found Returns Empty List
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to "/api/plants/category/999999" with the stored token
    Then the response status code should be 200
    And the response should be an empty JSON array

  @API-PLANT-GetCatUnauth-12
  Scenario: Verify Get Plants by Category Unauthorized
    When I send a GET request to get plants by first seeded category without authentication
    Then the response status code should be 401
    And the response should contain error field "status" with value 401
    And the response should contain error field "error" with value "UNAUTHORIZED"
    And the response should contain error field "message" with value "Unauthorized - Use Basic Auth or JWT"

  @API-PLANT-CreateForbidden-13
  Scenario: Verify Create Plant Under Category Forbidden
    Given I authenticate with username "testuser" and password "test123" and store the token
    When I create a new plant under an existing category
    Then the response status code should be 403
    And the response should contain error field "status" with value 403
    And the response should contain error field "error" with value "Forbidden"

  @API-PLANT-CreateSuccess-14
  Scenario: Verify Create Plant Under Category Success
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I create a new plant under an existing category
    Then the response status code should be 201
    And the response should contain plant details with field "id"
    And the response should contain plant details with field "name"
    And the response should contain plant details with field "price"
    And the response should contain plant details with field "quantity"
    And the response should contain nested field "category.id"
    And the response should contain nested field "category.name"

  @API-PLANT-CreateDup-15
  Scenario: Verify Create Plant Duplicate Request
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I create a new plant under an existing category
    And I create the same plant again
    Then the response status code should be 400
    And the response should contain error field "status" with value 400
    And the response should contain error field "error" containing "DUPLICATE"
    And the response should contain error field "message" containing "already exists"

  @API-PLANT-CreateNotFound-16
  Scenario: Verify Create Plant Category Not Found
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a POST request to "/api/plants/category/999999" with the stored token and payload:
      """
      {
        "id": 0,
        "name": "NonExistentPlant",
        "price": 10,
        "quantity": 1,
        "category": {
          "id": 999999,
          "name": "NonExistentCategory",
          "subCategories": []
        }
      }
      """
    Then the response status code should be 404
    And the response should contain error field "status" with value 404
    And the response should contain error field "error" containing "NOT_FOUND"
    And the response should contain error field "message" with value "Category not found"

  @API-PLANT-GetAll-17
  Scenario: Verify Get All Plants Success
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to "/api/plants" with the stored token
    Then the response status code should be 200
    And the response should contain plant details with field "[0].id"
    And the response should contain plant details with field "[0].name"
    And the response should contain plant details with field "[0].price"
    And the response should contain plant details with field "[0].quantity"
    And the response should contain nested field "[0].category.id"
    And the response should contain nested field "[0].category.name"

  @API-PLANT-GetAllParam-18
  Scenario: Verify Get All Plants With Unknown Query Param
    Given I authenticate with username "admin" and password "admin123" and store the token
    When I send a GET request to "/api/plants?invalidParam=true" with the stored token
    Then the response status code should be 200
    And the response should contain plant details with field "[0].id"
    And the response should contain plant details with field "[0].name"
    And the response should contain plant details with field "[0].price"
    And the response should contain plant details with field "[0].quantity"
    And the response should contain nested field "[0].category.id"
    And the response should contain nested field "[0].category.name"
