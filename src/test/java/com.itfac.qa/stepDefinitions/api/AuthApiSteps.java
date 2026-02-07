package com.itfac.qa.steps.api;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class AuthApiSteps {

    private Response response;

    @Given("the backend API is running at {string}")
    public void set_base_url(String url) {
        RestAssured.baseURI = Hooks.BASE_URL;
    }

    @When("I send a POST request to {string} with username {string} and password {string}")
    public void login(String endpoint, String username, String password) {
        response = given()
                .contentType("application/json")
                .body(Map.of("username", username, "password", password))
                .post(endpoint);
    }

    // UPDATED: Changed string to match the feature file update
    @Then("the login response status code should be {int}")
    public void verify_status_code(int statusCode) {
        Assert.assertEquals(statusCode, response.getStatusCode());
    }

    @Then("the response should contain a valid {string}")
    public void verify_token(String key) {
        String value = response.jsonPath().getString(key);
        Assert.assertNotNull("Token should not be null", value);
        Assert.assertFalse("Token should not be empty", value.isEmpty());
    }

    @Then("the response error message should be {string}")
    public void verify_error_message(String expectedMsg) {
        String body = response.getBody().asString();
        // Check if body contains the expected message (ignoring case)
        Assert.assertTrue("Body should contain: " + expectedMsg + " but got: " + body,
                body.toUpperCase().contains(expectedMsg.toUpperCase()));
    }
}