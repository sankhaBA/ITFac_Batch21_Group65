package com.itfac.qa.hooks;

import io.cucumber.java.Before;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;

public class ApiHooks {

    @Before("@API")
    public void setupApi() {
        RestAssured.filters(new AllureRestAssured());
    }
}
