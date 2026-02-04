package com.itfac.qa.stepDefinitions.api;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.Map;

public class CategoryApiSteps {

    private String BASE_URI = "http://localhost:8080";
    private String token;
    private Response response;
    private int currentCategoryId;

    // *** NEW: Variable to store the dynamic name we generated
    private String generatedName;

    @Given("I have a valid authentication token for {string}")
    public void i_have_token(String role) {
        String username = role.equalsIgnoreCase("admin") ? "admin" : "testuser";
        String password = role.equalsIgnoreCase("admin") ? "admin123" : "test123";

        Response authResponse = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("/api/auth/login");

        token = authResponse.jsonPath().getString("token");
        Assert.assertNotNull("Token was null! Login failed for user: " + username, token);
    }

    private RequestSpecification givenAuthenticated() {
        return RestAssured.given()
                .baseUri(BASE_URI)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON);
    }

    // *** NEW: Helper method to generate unique names
    // It appends a timestamp ONLY if the name is meant to be valid
    private String getUniqueName(String baseName) {
        // If name is null or empty, return it as is
        if (baseName == null || baseName.isEmpty() || baseName.equals("[empty]")) {
            return baseName;
        }

        // If it's a negative test case (Short name "AB" or Long name), DO NOT randomize
        // it
        if (baseName.length() < 3 || baseName.length() > 10) {
            return baseName;
        }

        // Generate a random suffix (last 4 digits of current time)
        String suffix = String.valueOf(System.currentTimeMillis() % 10000);

        // Ensure total length doesn't exceed 10 chars (API limit)
        // If base is "NewCat", suffix is "1234" -> "NewC1234"
        int maxBaseLen = 10 - suffix.length();
        if (baseName.length() > maxBaseLen) {
            baseName = baseName.substring(0, maxBaseLen);
        }

        return baseName + suffix;
    }

    @When("I send a GET request to {string}")
    public void i_send_get(String endpoint) {
        response = givenAuthenticated().get(endpoint);
    }

    @Then("the response status code should be {int}")
    public void status_code_should_be(int code) {
        Assert.assertEquals(code, response.getStatusCode());
    }

    @Then("the response should contain a list of categories")
    public void response_contains_list() {
        Assert.assertTrue(response.jsonPath().getList("$").size() >= 0);
    }

    @When("I send a POST request to {string} with the following body:")
    public void i_send_post(String endpoint, DataTable dataTable) {
        Map<String, String> data = dataTable.asMap(String.class, String.class);
        String name = data.get("name");

        // *** NEW: Generate unique name and store it
        generatedName = getUniqueName(name);

        String jsonBody = "{" +
                "\"name\": \"" + generatedName + "\"," +
                "\"parent\": null" +
                "}";

        response = givenAuthenticated()
                .body(jsonBody)
                .post(endpoint);

        if (response.getStatusCode() == 201) {
            currentCategoryId = response.jsonPath().getInt("id");
        }
    }

    @Then("the response body should contain {string} with value {string}")
    public void body_contains(String key, String expectedValue) {
        String actualValue = response.jsonPath().getString(key);

        // *** NEW: Smart Assertion
        // If we are checking "name", we compare against the generatedName (e.g.
        // "NewCat8392")
        // instead of the static feature file value ("NewCat")
        if (key.equals("name") && generatedName != null && expectedValue.length() >= 3
                && expectedValue.length() <= 10) {
            // Check if actual value starts with the expected base name
            Assert.assertEquals(generatedName, actualValue);
        } else {
            Assert.assertEquals(expectedValue, actualValue);
        }
    }

    @Given("a category exists with name {string}")
    public void category_exists(String name) {
        // *** NEW: Always randomize setup data
        String uniqueName = getUniqueName(name);

        String jsonBody = "{\"name\": \"" + uniqueName + "\"}";
        Response setupResp = givenAuthenticated().body(jsonBody).post("/api/categories");

        if (setupResp.getStatusCode() >= 400) {
            throw new RuntimeException(
                    "Setup Failed. Status: " + setupResp.getStatusCode() + " Msg: " + setupResp.getBody().asString());
        }
        currentCategoryId = setupResp.jsonPath().getInt("id");
    }

    @Given("a category exists with name {string} \\(created by admin)")
    public void category_exists_admin_context(String name) {
        // 1. Get Admin Token
        String adminToken = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
                .post("/api/auth/login")
                .jsonPath()
                .getString("token");

        // *** NEW: Always randomize setup data
        String uniqueName = getUniqueName(name);

        // 2. Create Category
        Response setupResp = RestAssured.given()
                .baseUri(BASE_URI)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + uniqueName + "\"}")
                .post("/api/categories");

        if (setupResp.getStatusCode() >= 400) {
            throw new RuntimeException("Setup Failed (Admin). Status: " + setupResp.getStatusCode() + " Msg: "
                    + setupResp.getBody().asString());
        }

        currentCategoryId = setupResp.jsonPath().getInt("id");
    }

    @When("I send a PUT request to update the category with name {string}")
    public void i_send_put(String newName) {
        // *** NEW: Generate unique name for update
        generatedName = getUniqueName(newName);

        String jsonBody = "{\"name\": \"" + generatedName + "\"}";
        response = givenAuthenticated()
                .body(jsonBody)
                .put("/api/categories/" + currentCategoryId);
    }

    @When("I send a DELETE request for that category")
    public void i_send_delete() {
        response = givenAuthenticated().delete("/api/categories/" + currentCategoryId);
    }
}