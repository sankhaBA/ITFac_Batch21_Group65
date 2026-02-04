package com.itfac.qa.steps;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;
import java.util.HashMap;
import java.util.Map;

public class PlantsApiSteps {

    private static Response response;
    private static String authToken;

    @Given("I authenticate with username {string} and password {string} and store the token")
    public void authenticateAndStoreToken(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        Response loginResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login");

        Assert.assertEquals("Login failed! Status code mismatch.", 200, loginResponse.getStatusCode());
        
        authToken = loginResponse.jsonPath().getString("token");
        Assert.assertNotNull("Token not found in login response!", authToken);
        Assert.assertFalse("Token is empty!", authToken.isEmpty());
        
        System.out.println("Authentication successful. Token obtained: " + authToken.substring(0, 20) + "...");
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

    @Then("the response should contain nested field {string}")
    public void verifyNestedFieldExists(String fieldPath) {
        Object fieldValue = response.jsonPath().get(fieldPath);
        Assert.assertNotNull("Nested field '" + fieldPath + "' is missing from response!", fieldValue);
        System.out.println("Nested field '" + fieldPath + "' found with value: " + fieldValue);
    }
}
