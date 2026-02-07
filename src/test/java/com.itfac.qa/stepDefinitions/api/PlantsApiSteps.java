package com.itfac.qa.stepDefinitions.api;

import com.itfac.qa.runners.TestRunner;
import com.itfac.qa.utils.AuthenticationHelper;
import com.itfac.qa.utils.ConfigurationManager;
import com.itfac.qa.utils.TestDataSeeder;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlantsApiSteps {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private final AuthenticationHelper authHelper = new AuthenticationHelper();

    private static Response response;
    private static String authToken;

    private static String lastPostEndpoint;
    private static String lastPostPayload;
    private static Long firstSeededPlantId;
    private static Long lastSeededPlantId;

    // Helper method to get first seeded plant ID
    private Long getFirstSeededPlantId() {
        if (firstSeededPlantId == null) {
            TestDataSeeder seeder = TestRunner.getDataSeeder();
            if (seeder != null) {
                List<Long> plantIds = seeder.getSeededIds("plants");
                if (!plantIds.isEmpty()) {
                    firstSeededPlantId = plantIds.get(0);
                }
            }
        }
        return firstSeededPlantId;
    }

    // Helper method to get last seeded plant ID
    private Long getLastSeededPlantId() {
        if (lastSeededPlantId == null) {
            TestDataSeeder seeder = TestRunner.getDataSeeder();
            if (seeder != null) {
                List<Long> plantIds = seeder.getSeededIds("plants");
                if (!plantIds.isEmpty()) {
                    lastSeededPlantId = plantIds.get(plantIds.size() - 1);
                }
            }
        }
        return lastSeededPlantId;
    }

    // Helper method to get first seeded category ID
    private Long getFirstSeededCategoryId() {
        TestDataSeeder seeder = TestRunner.getDataSeeder();
        if (seeder != null) {
            List<Long> categoryIds = seeder.getSeededIds("categories");
            if (!categoryIds.isEmpty()) {
                return categoryIds.get(0);
            }
        }
        return null;
    }

    @Given("I authenticate with username {string} and password {string} and store the token")
    public void authenticateAndStoreToken(String username, String password) {
        authToken = authHelper.authenticate(username, password);
        System.out.println("Authentication successful. Token obtained: " + authToken.substring(0, 20) + "...");
    }

    @When("I send a GET request to get the first seeded plant by ID")
    public void sendGetRequestForFirstSeededPlant() {
        Long plantId = getFirstSeededPlantId();
        Assert.assertNotNull("No seeded plants found!", plantId);
        sendGetRequestWithToken("/api/plants/" + plantId);
    }

    @When("I send a PUT request to update the first seeded plant with name {string} price {int} and quantity {int}")
    public void sendPutRequestToUpdateFirstSeededPlant(String name, int price, int quantity) {
        Long plantId = getFirstSeededPlantId();
        Assert.assertNotNull("No seeded plants found!", plantId);
        
        // Get category info from existing plant
        Map<String, Object> existing = fetchExistingCategoryFromPlants();
        int categoryId = (int) existing.get("categoryId");
        String categoryName = (String) existing.get("categoryName");
        
        String payload = String.format("{"
                + "\"id\": %d,"
                + "\"name\": \"%s\","
                + "\"price\": %d,"
                + "\"quantity\": %d,"
                + "\"category\": {"
                + "\"id\": %d,"
                + "\"name\": \"%s\","
                + "\"parent\": null,"
                + "\"subCategories\": []"
                + "}"
                + "}", plantId, name, price, quantity, categoryId, categoryName);
        
        sendPutRequestWithTokenAndPayload("/api/plants/" + plantId, payload);
    }

    @When("I send a DELETE request to delete the last seeded plant by ID")
    public void sendDeleteRequestForLastSeededPlant() {
        Long plantId = getLastSeededPlantId();
        Assert.assertNotNull("No seeded plants found!", plantId);
        sendDeleteRequestWithToken("/api/plants/" + plantId);
    }

    @When("I send a DELETE request to delete first seeded plant without authentication")
    public void sendDeleteRequestForFirstSeededPlantWithoutAuth() {
        Long plantId = getFirstSeededPlantId();
        Assert.assertNotNull("No seeded plants found!", plantId);
        sendDeleteRequestWithoutAuth("/api/plants/" + plantId);
    }

    @When("I send a GET request to get plants by first seeded category without authentication")
    public void sendGetRequestByFirstSeededCategoryWithoutAuth() {
        Long categoryId = getFirstSeededCategoryId();
        Assert.assertNotNull("No seeded categories found!", categoryId);
        sendGetRequestWithoutAuth("/api/plants/category/" + categoryId);
    }

    @When("I send a GET request to {string} with the stored token")
    public void sendGetRequestWithToken(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .when()
                .get(endpoint);
        
        System.out.println("GET request sent to: " + endpoint);
        System.out.println("Response Status: " + response.getStatusCode());
        
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a GET request to {string} with the stored token using an existing category id")
    public void sendGetRequestWithTokenUsingExistingCategoryId(String endpointTemplate) {
        int categoryId = fetchExistingCategoryIdFromPlants();
        String endpoint = endpointTemplate.replace("{categoryId}", String.valueOf(categoryId));
        sendGetRequestWithToken(endpoint);
    }

    @When("I send a GET request to {string} without authentication")
    public void sendGetRequestWithoutAuth(String endpoint) {
        response = RestAssured.given()
                .header("Accept", "application/json")
                .when()
                .get(endpoint);
        
        System.out.println("GET request sent to: " + endpoint + " (without authentication)");
        System.out.println("Response Status: " + response.getStatusCode());
        
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a PUT request to {string} with the stored token and payload:")
    public void sendPutRequestWithTokenAndPayload(String endpoint, String payload) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(endpoint);
        
        System.out.println("PUT request sent to: " + endpoint);
        System.out.println("Response Status: " + response.getStatusCode());
        
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a POST request to {string} with the stored token and payload:")
    public void sendPostRequestWithTokenAndPayload(String endpoint, String payload) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(endpoint);

        System.out.println("POST request sent to: " + endpoint);
        System.out.println("Response Status: " + response.getStatusCode());

        AuthApiSteps.setSharedResponse(response);
    }

    @When("I create a new plant under an existing category")
    public void createNewPlantUnderExistingCategory() {
        Map<String, Object> existing = fetchExistingCategoryFromPlants();
        int categoryId = (int) existing.get("categoryId");
        String categoryName = (String) existing.get("categoryName");

        // backend validation: name must be between 3 and 25 characters
        String plantName = "Auto_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String payload = buildCreatePlantPayload(plantName, categoryId, categoryName);

        lastPostEndpoint = "/api/plants/category/" + categoryId;
        lastPostPayload = payload;

        response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(lastPostEndpoint);

        System.out.println("POST request sent to: " + lastPostEndpoint);
        System.out.println("Response Status: " + response.getStatusCode());

        AuthApiSteps.setSharedResponse(response);
    }

    @When("I create the same plant again")
    public void createSamePlantAgain() {
        Assert.assertNotNull("No previous POST endpoint stored. Call create step first.", lastPostEndpoint);
        Assert.assertNotNull("No previous POST payload stored. Call create step first.", lastPostPayload);

        response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .body(lastPostPayload)
                .when()
                .post(lastPostEndpoint);

        System.out.println("POST request re-sent to: " + lastPostEndpoint);
        System.out.println("Response Status: " + response.getStatusCode());

        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a DELETE request to {string} with the stored token")
    public void sendDeleteRequestWithToken(String endpoint) {
        response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .when()
                .delete(endpoint);
        
        System.out.println("DELETE request sent to: " + endpoint);
        System.out.println("Response Status: " + response.getStatusCode());
        
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a DELETE request to {string} without authentication")
    public void sendDeleteRequestWithoutAuth(String endpoint) {
        response = RestAssured.given()
                .header("Accept", "application/json")
                .when()
                .delete(endpoint);
        
        System.out.println("DELETE request sent to: " + endpoint + " (without authentication)");
        System.out.println("Response Status: " + response.getStatusCode());
        
        AuthApiSteps.setSharedResponse(response);
    }

    @Then("the response should contain plant details with field {string}")
    public void verifyPlantFieldExists(String fieldName) {
        Object fieldValue = response.jsonPath().get(fieldName);
        Assert.assertNotNull("Field '" + fieldName + "' is missing from response!", fieldValue);
        System.out.println("Field '" + fieldName + "' found with value: " + fieldValue);
    }

    @Then("the response should contain error field {string} with value {int}")
    public void verifyErrorFieldInt(String fieldName, int expectedValue) {
        int actualValue = response.jsonPath().getInt(fieldName);
        Assert.assertEquals("Field '" + fieldName + "' value mismatch!", expectedValue, actualValue);
        System.out.println("Field '" + fieldName + "' verified with value: " + actualValue);
    }

    @Then("the response should contain error field {string} with value {string}")
    public void verifyErrorFieldString(String fieldName, String expectedValue) {
        String actualValue = response.jsonPath().getString(fieldName);
        Assert.assertEquals("Field '" + fieldName + "' value mismatch!", expectedValue, actualValue);
        System.out.println("Field '" + fieldName + "' verified with value: " + actualValue);
    }

    @Then("the response should contain error field {string} containing {string}")
    public void verifyErrorFieldStringContains(String fieldName, String expectedSubstring) {
        String actualValue = response.jsonPath().getString(fieldName);
        Assert.assertNotNull("Field '" + fieldName + "' is missing from response!", actualValue);
        Assert.assertTrue(
                "Field '" + fieldName + "' did not contain expected text. Actual: '" + actualValue + "'",
                actualValue.contains(expectedSubstring)
        );
    }

    @Then("the response should contain nested field {string}")
    public void verifyNestedFieldExists(String fieldPath) {
        Object fieldValue = response.jsonPath().get(fieldPath);
        Assert.assertNotNull("Nested field '" + fieldPath + "' is missing from response!", fieldValue);
        System.out.println("Nested field '" + fieldPath + "' found with value: " + fieldValue);
    }

    @Then("the response body should be empty")
    public void verifyResponseBodyEmpty() {
        String body = (response != null && response.getBody() != null) ? response.getBody().asString() : null;
        Assert.assertNotNull("No response body available to verify.", body);
        Assert.assertTrue("Expected empty response body but got: '" + body + "'", body.trim().isEmpty());
    }

    @Then("the response should be an empty JSON array")
    public void verifyResponseIsEmptyJsonArray() {
        String body = (response != null && response.getBody() != null) ? response.getBody().asString() : null;
        Assert.assertNotNull("No response body available to verify.", body);
        Assert.assertEquals("Expected an empty JSON array.", "[]", body.trim());
    }

    private int fetchExistingCategoryIdFromPlants() {
        Map<String, Object> existing = fetchExistingCategoryFromPlants();
        return (int) existing.get("categoryId");
    }

    private Map<String, Object> fetchExistingCategoryFromPlants() {
        Response plantsResponse = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .when()
                .get("/api/plants");

        Assert.assertEquals("Failed to fetch plants list.", 200, plantsResponse.getStatusCode());

        Integer categoryId = plantsResponse.jsonPath().getInt("[0].category.id");
        String categoryName = plantsResponse.jsonPath().getString("[0].category.name");
        Assert.assertNotNull("No plants returned from /api/plants; cannot infer existing category.", categoryId);
        Assert.assertNotNull("Category name missing from /api/plants[0].category.name.", categoryName);

        Map<String, Object> result = new HashMap<>();
        result.put("categoryId", categoryId);
        result.put("categoryName", categoryName);
        return result;
    }

    private String buildCreatePlantPayload(String plantName, int categoryId, String categoryName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", 0);
        payload.put("name", plantName);
        payload.put("price", 10);
        payload.put("quantity", 1);

        Map<String, Object> category = new HashMap<>();
        category.put("id", categoryId);
        category.put("name", categoryName);
        category.put("subCategories", Collections.emptyList());
        payload.put("category", category);

        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build create-plant JSON payload", e);
        }
    }
}
