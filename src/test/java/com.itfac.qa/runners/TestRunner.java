package com.itfac.qa.runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", glue = { "com.itfac.qa.stepDefinitions",
                "com.itfac.qa.hooks" }, tags = "@UI", plugin = { "pretty",
                                "html:target/cucumber-reports.html" }, monochrome = true)
public class TestRunner {
}
