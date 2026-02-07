package com.itfac.qa.steps.api;

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
    private static String lastCreatedSaleId; // Static to share across steps in same scenario

    @Given("the application is running")
    public void the_application_is_running() {
        RestAssured.baseURI = Hooks.BASE_URL;
    }

    @Given("I am logged in as {string} via API")
    public void i_am_logged_in_as_via_api(String role) {
        String username = role.equalsIgnoreCase("Admin") ? "admin" : "testuser";
        String password = role.equalsIgnoreCase("Admin") ? "admin123" : "test123";

        Response loginResp = given()
                .contentType("application/json")
                .body(Map.of("username", username, "password", password))
                .post("/api/auth/login");

        this.jwtToken = loginResp.jsonPath().getString("token");
        this.request = given().header("Authorization", "Bearer " + jwtToken);
    }

    @When("I send a GET request to {string}")
    public void i_send_a_get_request_to(String endpoint) {
        response = request.get(endpoint);
    }

    @When("I request to sell plant ID {string} with quantity {string}")
    public void i_request_to_sell_plant(String plantId, String quantity) {
        response = request.queryParam("quantity", quantity).post("/api/sales/plant/" + plantId);

        // Capture ID if created successfully
        if (response.getStatusCode() == 201) {
            lastCreatedSaleId = response.jsonPath().getString("id");
        }
    }

    @When("I note the sale ID")
    public void i_note_the_sale_id() {
        Assert.assertNotNull("Sale ID should not be null", lastCreatedSaleId);
    }

    @When("I send a DELETE request to that sale ID")
    public void i_send_delete_to_saved_id() {
        response = request.delete("/api/sales/" + lastCreatedSaleId);
    }

    @When("I send a DELETE request to sale ID {string}")
    public void i_send_delete_to_specific_id(String id) {
        response = request.delete("/api/sales/" + id);
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

    @Then("the sales API error message should be {string}")
    public void api_error_check(String msg) {
        String body = response.getBody().asString();
        Assert.assertTrue(body.contains(msg));
    }
}