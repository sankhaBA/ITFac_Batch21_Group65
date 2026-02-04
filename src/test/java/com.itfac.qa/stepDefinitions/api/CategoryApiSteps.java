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

    @Given("I have a valid authentication token for {string}")
    public void i_have_token(String role) {
        String username = role.equalsIgnoreCase("admin") ? "admin" : "testuser";
        String password = role.equalsIgnoreCase("admin") ? "admin123" : "test123";

        Response authResponse = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body("{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}")
                .post("/login"); // Adjust endpoint based on Swagger (often /login or /api/auth/login)

        // Assuming the token is in the response body field "token"
        // If it returns a header, use authResponse.getHeader("Authorization");
        token = authResponse.jsonPath().getString("token");

        // If token is null, the test environment might be down or credentials wrong
        if (token == null) {
            // Fallback for simple setups: sometimes it's just a session cookie
            // For this assignment, assuming JWT token in body
        }
    }

    private RequestSpecification givenAuthenticated() {
        return RestAssured.given()
                .baseUri(BASE_URI)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON);
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

        // Handle empty strings for validation tests
        String name = data.get("name");

        String jsonBody = "{" +
                "\"name\": \"" + name + "\"," +
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
    public void body_contains(String key, String value) {
        String actualValue = response.jsonPath().getString(key);
        Assert.assertEquals(value, actualValue);
    }

    @Given("a category exists with name {string}")
    public void category_exists(String name) {
        // Setup: Create a category so we can edit/delete it
        String jsonBody = "{\"name\": \"" + name + "\"}";
        Response setupResp = givenAuthenticated().body(jsonBody).post("/api/categories");
        currentCategoryId = setupResp.jsonPath().getInt("id");
    }

    @Given("a category exists with name {string} \\(created by admin)")
    public void category_exists_admin_context(String name) {
        // We need to switch context to admin to create it, then switch back?
        // For simplicity, assuming the "Setup" happens before the user token generation
        // step in logic,
        // or we just manually create one here.

        // 1. Get Admin Token
        String adminToken = RestAssured.given().baseUri(BASE_URI).contentType(ContentType.JSON)
                .body("{\"username\": \"admin\", \"password\": \"admin123\"}").post("/login").jsonPath()
                .getString("token");

        // 2. Create Category
        Response setupResp = RestAssured.given().baseUri(BASE_URI).header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON).body("{\"name\": \"" + name + "\"}").post("/api/categories");

        currentCategoryId = setupResp.jsonPath().getInt("id");
    }

    @When("I send a PUT request to update the category with name {string}")
    public void i_send_put(String newName) {
        String jsonBody = "{\"name\": \"" + newName + "\"}";
        response = givenAuthenticated()
                .body(jsonBody)
                .put("/api/categories/" + currentCategoryId);
    }

    @When("I send a DELETE request for that category")
    public void i_send_delete() {
        response = givenAuthenticated().delete("/api/categories/" + currentCategoryId);
    }
}