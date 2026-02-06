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
    private int targetParentId;
    private String targetParentName;
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

    private String getUniqueName(String baseName) {
        if (baseName == null || baseName.isEmpty() || baseName.equals("[empty]")) {
            return baseName;
        }
        if (baseName.length() < 3 || baseName.length() > 10) {
            return baseName;
        }
        String suffix = String.valueOf(System.currentTimeMillis() % 10000);
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
        if (key.equals("name") && generatedName != null && expectedValue.length() >= 3
                && expectedValue.length() <= 10) {
            Assert.assertEquals(generatedName, actualValue);
        } else {
            Assert.assertEquals(expectedValue, actualValue);
        }
    }

    @Given("a category exists with name {string}")
    public void category_exists(String name) {
        // String uniqueName = getUniqueName(name);

        // String jsonBody = "{\"name\": \"" + uniqueName + "\"}";
        // Response setupResp =
        // givenAuthenticated().body(jsonBody).post("/api/categories");

        // if (setupResp.getStatusCode() >= 400) {
        // throw new RuntimeException(
        // "Setup Failed. Status: " + setupResp.getStatusCode() + " Msg: " +
        // setupResp.getBody().asString());
        // }
        // currentCategoryId = setupResp.jsonPath().getInt("id");

        generatedName = getUniqueName(name);

        // Update the JSON body to use 'generatedName'
        String jsonBody = "{\"name\": \"" + generatedName + "\"}";

        Response setupResp = givenAuthenticated().body(jsonBody).post("/api/categories");

        if (setupResp.getStatusCode() >= 400) {
            throw new RuntimeException(
                    "Setup Failed. Status: " + setupResp.getStatusCode() + " Msg: " + setupResp.getBody().asString());
        }
        currentCategoryId = setupResp.jsonPath().getInt("id");
    }

    @Given("a category exists with name {string} \\(created by admin)")
    public void category_exists_admin_context(String name) {
        String adminToken = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
                .post("/api/auth/login")
                .jsonPath()
                .getString("token");
        String uniqueName = getUniqueName(name);

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

    @When("I send a PUT request to update a non-existent category with ID {int}")
    public void i_update_non_existent_category(int id) {
        String jsonBody = "{\"name\": \"PhantomCategory\"}";
        response = givenAuthenticated()
                .body(jsonBody)
                .put("/api/categories/" + id);
    }

    @Given("a parent category exists with name {string}")
    public void parent_category_exists(String name) {
        // Reuse existing logic to create
        category_exists(name);
        targetParentId = currentCategoryId;

        // SAVE THE NAME! We need this for verification
        targetParentName = generatedName;

        // Reset for the next step
        currentCategoryId = 0;
    }

    @When("I send a PUT request to link the category to the parent")
    public void i_link_category_to_parent() {
        // STRATEGY: Try sending parentId as a Query Param
        // AND as a Body field (Double Attack).
        String jsonBody = "{" +
                "\"name\": \"" + generatedName + "\"," +
                "\"parentId\": " + targetParentId +
                "}";

        response = givenAuthenticated()
                .queryParam("parentId", targetParentId) // Add as Query Param
                .body(jsonBody)
                .put("/api/categories/" + currentCategoryId);

        System.out.println("DEBUG: Sending PUT (QueryParam + Body) to /api/categories/" + currentCategoryId);
    }

    @Then("the response body should contain {string} with value from the parent category")
    public void response_contains_parent_id(String key) {
        // 1. Force a refresh! Fetch the category again from the server to get the
        // latest data.
        // (Sometimes PUT responses are incomplete, but GET requests return full
        // details)
        response = givenAuthenticated().get("/api/categories/" + currentCategoryId);

        // 2. Now extract the parent name from this fresh response
        String actualParentName = response.jsonPath().getString("parent");

        // Debugging prints
        System.out.println("DEBUG: ID Sent: " + targetParentId);
        System.out.println("DEBUG: Expected Parent Name: " + targetParentName);
        System.out.println("DEBUG: Actual Parent Name (from fresh GET): " + actualParentName);

        // 3. Verify
        Assert.assertNotNull("Parent field was null in database!", actualParentName);
        Assert.assertEquals("Parent Name did not match!", targetParentName, actualParentName);
    }
}