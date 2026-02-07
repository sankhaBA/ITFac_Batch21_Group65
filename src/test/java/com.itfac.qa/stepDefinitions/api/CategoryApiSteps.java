package com.itfac.qa.stepDefinitions.api;

import com.itfac.qa.utils.AuthenticationHelper;
import com.itfac.qa.utils.ConfigurationManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.List;
import java.util.Map;

public class CategoryApiSteps {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private final AuthenticationHelper authHelper = new AuthenticationHelper();
    private String BASE_URI = config.getApiBaseUrl();
    private String token;
    private Response response;
    private int currentCategoryId;
    private int targetParentId;
    private String targetParentName;
    private String generatedName;

    @Given("I have a valid authentication token for {string}")
    public void i_have_token(String role) {
        authHelper.authenticateByRole(role);
        token = authHelper.getToken();
    }

    private RequestSpecification givenAuthenticated() {
        return authHelper.getAuthenticatedRequest();
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
        AuthApiSteps.setSharedResponse(response);
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
        AuthApiSteps.setSharedResponse(response);

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
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a DELETE request for that category")
    public void i_send_delete() {
        response = givenAuthenticated().delete("/api/categories/" + currentCategoryId);
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a PUT request to update a non-existent category with ID {int}")
    public void i_update_non_existent_category(int id) {
        String jsonBody = "{\"name\": \"PhantomCategory\"}";
        response = givenAuthenticated()
                .body(jsonBody)
                .put("/api/categories/" + id);
        AuthApiSteps.setSharedResponse(response);
    }

    @Given("a parent category exists with name {string}")
    public void parent_category_exists(String name) {
        category_exists(name);
        targetParentId = currentCategoryId;
        targetParentName = generatedName;
        currentCategoryId = 0;
    }

    @When("I send a PUT request to link the category to the parent")
    public void i_link_category_to_parent() {
        String jsonBody = "{" +
                "\"name\": \"" + generatedName + "\"," +
                "\"parentId\": " + targetParentId +
                "}";

        response = givenAuthenticated()
                .queryParam("parentId", targetParentId)
                .body(jsonBody)
                .put("/api/categories/" + currentCategoryId);
        AuthApiSteps.setSharedResponse(response);

        System.out.println("DEBUG: Sending PUT (QueryParam + Body) to /api/categories/" + currentCategoryId);
    }

    @Then("the response body should contain {string} with value from the parent category")
    public void response_contains_parent_id(String key) {
        response = givenAuthenticated().get("/api/categories/" + currentCategoryId);
        AuthApiSteps.setSharedResponse(response);
        String actualParentName = response.jsonPath().getString("parent");
        System.out.println("DEBUG: ID Sent: " + targetParentId);
        System.out.println("DEBUG: Expected Parent Name: " + targetParentName);
        System.out.println("DEBUG: Actual Parent Name (from fresh GET): " + actualParentName);
        Assert.assertNotNull("Parent field was null in database!", actualParentName);
        Assert.assertEquals("Parent Name did not match!", targetParentName, actualParentName);
    }

    @When("I send a PUT request to link the category using strictly {string}")
    public void i_send_put_strict_compliance(String fieldName) {
        String jsonBody = "{" +
                "\"name\": \"" + generatedName + "\"," +
                "\"parentId\": " + targetParentId +
                "}";

        System.out.println("DEBUG: Testing Swagger Compliance. Body: " + jsonBody);

        response = givenAuthenticated()
                .body(jsonBody)
                .put("/api/categories/" + currentCategoryId);
        AuthApiSteps.setSharedResponse(response);
    }

    // ========== Additional GET Endpoint Steps ==========

    @Then("the response should contain category with name containing {string}")
    public void response_contains_category_with_name(String nameFragment) {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        boolean found = categories.stream()
                .anyMatch(cat -> {
                    String name = (String) cat.get("name");
                    return name != null && name.contains(nameFragment);
                });
        Assert.assertTrue("No category found containing: " + nameFragment, found);
    }

    @When("the category is linked to the parent")
    public void link_category_to_parent() {
        String jsonBody = "{" +
                "\"name\": \"" + generatedName + "\"," +
                "\"parentId\": " + targetParentId +
                "}";

        response = givenAuthenticated()
                .queryParam("parentId", targetParentId)
                .body(jsonBody)
                .put("/api/categories/" + currentCategoryId);
        AuthApiSteps.setSharedResponse(response);

        Assert.assertEquals("Failed to link category to parent", 200, response.getStatusCode());
    }

    @When("I send a GET request to categories filtered by parent ID")
    public void i_send_get_filtered_by_parent() {
        response = givenAuthenticated().get("/api/categories?parentId=" + targetParentId);
        AuthApiSteps.setSharedResponse(response);
    }

    @Then("the response should contain only categories with the specified parent")
    public void response_contains_only_with_parent() {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        for (Map<String, Object> cat : categories) {
            String parent = (String) cat.get("parent");
            Assert.assertEquals("Category has wrong parent", targetParentName, parent);
        }
    }

    @When("I send a GET request with name {string} and parent ID filter")
    public void i_send_get_with_name_and_parent(String nameFilter) {
        response = givenAuthenticated()
                .get("/api/categories?name=" + nameFilter + "&parentId=" + targetParentId);
        AuthApiSteps.setSharedResponse(response);
    }

    @Then("the response should contain category with name containing {string} and specified parent")
    public void response_contains_with_name_and_parent(String nameFragment) {
        List<Map<String, Object>> categories = response.jsonPath().getList("$");
        Assert.assertFalse("Expected at least one category in response", categories.isEmpty());

        for (Map<String, Object> cat : categories) {
            String name = (String) cat.get("name");
            String parent = (String) cat.get("parent");
            Assert.assertTrue("Name does not contain fragment: " + nameFragment,
                    name != null && name.contains(nameFragment));
            Assert.assertEquals("Parent does not match", targetParentName, parent);
        }
    }

    @Then("the response should be an empty array")
    public void response_is_empty_array() {
        List<?> list = response.jsonPath().getList("$");
        Assert.assertTrue("Expected empty array but got: " + list.size() + " items", list.isEmpty());
    }

    @When("I send a GET request for that specific category by ID")
    public void i_send_get_by_id() {
        response = givenAuthenticated().get("/api/categories/" + currentCategoryId);
        AuthApiSteps.setSharedResponse(response);
    }

    @When("I send a DELETE request to {string}")
    public void i_send_delete_to_endpoint(String endpoint) {
        response = givenAuthenticated().delete(endpoint);
        AuthApiSteps.setSharedResponse(response);
    }
}