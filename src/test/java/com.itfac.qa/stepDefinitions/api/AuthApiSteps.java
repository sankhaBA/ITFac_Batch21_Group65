package com.itfac.qa.steps;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Assert;
import java.util.HashMap;
import java.util.Map;

public class AuthApiSteps {

    private Response response;

    @Given("the backend API is running at {string}")
    public void setBaseUrl(String url) {
        RestAssured.baseURI = url;
    }

    @When("I send a POST request to {string} with username {string} and password {string}")
    public void sendLoginRequest(String endpoint, String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post(endpoint);
    }

    @Then("the response status code should be {int}")
    public void verifyStatusCode(int expectedCode) {
        Assert.assertEquals("Status code mismatch!", expectedCode, response.getStatusCode());
    }

    @Then("the response should contain a valid {string}")
    public void verifyTokenExists(String field) {
        String value = response.jsonPath().getString(field);
        Assert.assertNotNull("Field " + field + " is missing from response!", value);
        Assert.assertFalse("Field " + field + " is empty!", value.isEmpty());
    }

    @Then("the response error message should be {string}")
    public void verifyErrorMessage(String expectedError) {
        String actualError = response.jsonPath().getString("error");
        Assert.assertEquals("Error message mismatch!", expectedError, actualError);
    }
}