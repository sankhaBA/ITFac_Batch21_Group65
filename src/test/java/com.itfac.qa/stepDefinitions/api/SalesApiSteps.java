package com.itfac.qa.stepDefinitions.api;

import com.itfac.qa.hooks.Hooks;
import com.itfac.qa.runners.TestRunner;
import com.itfac.qa.utils.AuthenticationHelper;
import com.itfac.qa.utils.ConfigurationManager;
import com.itfac.qa.utils.TestDataSeeder;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class SalesApiSteps {
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private final AuthenticationHelper authHelper = new AuthenticationHelper();
    private String BASE_URI = config.getApiBaseUrl();
    private Response response;
    private RequestSpecification request;
    private String jwtToken;
    private static String lastCreatedSaleId; // Static to share across steps in same scenario

    // Helper method to get first seeded plant ID
    private Long getFirstSeededPlantId() {
        TestDataSeeder seeder = TestRunner.getDataSeeder();
        if (seeder != null) {
            List<Long> plantIds = seeder.getSeededIds("plants");
            if (!plantIds.isEmpty()) {
                return plantIds.get(0);
            }
        }
        return null;
    }

    @Given("the application is running")
    public void the_application_is_running() {
        RestAssured.baseURI = BASE_URI;
    }

    @Given("I am logged in as {string} via API")
    public void i_am_logged_in_as_via_api(String role) {
        this.jwtToken = authHelper.authenticateByRole(role);
        this.request = authHelper.getAuthenticatedRequest();
    }

    @When("I send a GET request to sales API {string}")
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

    @When("I request to sell the first seeded plant with quantity {string}")
    public void i_request_to_sell_first_seeded_plant(String quantity) {
        Long plantId = getFirstSeededPlantId();
        Assert.assertNotNull("No seeded plants found!", plantId);
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

    @Then("the sales API response status code should be {int}")
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