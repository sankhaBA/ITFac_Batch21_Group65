package com.itfac.qa.stepDefinitions.api;

import com.itfac.qa.hooks.Hooks;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class SalesApiSteps {

    private Response response;
    private RequestSpecification request;
    private String jwtToken;

    @Given("the application is running")
    public void the_application_is_running() {
        RestAssured.baseURI = Hooks.BASE_URL;
    }

    @Given("I am logged in as {string} via API")
    public void i_am_logged_in_as_via_api(String role) {
        String username = role.equalsIgnoreCase("Admin") ? "admin" : "testuser";
        String password = role.equalsIgnoreCase("Admin") ? "admin123" : "test123";

        try {
            Response loginResp = given()
                    .contentType("application/json")
                    .body(Map.of("username", username, "password", password))
                    .post("/api/auth/login");

            this.jwtToken = loginResp.jsonPath().getString("token");
            this.request = given().header("Authorization", "Bearer " + jwtToken);
        } catch (Exception e) {
            System.out.println("API Login failed: " + e.getMessage());
        }
    }

    @When("I send a GET request to {string}")
    public void i_send_a_get_request_to(String endpoint) {
        response = request.get(endpoint);
    }

    @When("I request to sell plant ID {string} with quantity {string}")
    public void i_request_to_sell_plant(String plantId, String quantity) {
        response = request
                .queryParam("quantity", quantity)
                .post("/api/sales/plant/" + plantId);
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(int statusCode) {
        Assert.assertEquals(statusCode, response.getStatusCode());
    }

    @Then("the response should contain a list of sales")
    public void the_response_should_contain_a_list_of_sales() {
        List<Object> sales = response.jsonPath().getList("$");
        Assert.assertNotNull(sales);
    }
}