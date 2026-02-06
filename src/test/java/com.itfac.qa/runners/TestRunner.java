package com.itfac.qa.runners;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        // FIXED: changed '.steps' to '.stepDefinitions' to match your folder
        glue = { "com.itfac.qa.stepDefinitions", "com.itfac.qa.hooks" },
        tags = "@UI or @API",
        plugin = { "pretty", "html:target/cucumber-reports.html" },
        monochrome = true
)
public class TestRunner {
}