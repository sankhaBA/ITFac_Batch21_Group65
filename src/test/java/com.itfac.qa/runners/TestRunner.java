package com.itfac.qa.runners;

import com.itfac.qa.utils.ConfigurationManager;
import com.itfac.qa.utils.TestDataSeeder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features", glue = { "com.itfac.qa.stepDefinitions",
        "com.itfac.qa.hooks" }, tags = "@UI or @API", plugin = { "pretty",
                "html:target/cucumber-reports.html" }, monochrome = true)
public class TestRunner {

    private static TestDataSeeder dataSeeder;
    private static final ConfigurationManager config = ConfigurationManager.getInstance();

    /**
     * Seed test data once before all test scenarios.
     * Runs at the start of the test session regardless of filter tags.
     */
    @BeforeClass
    public static void setupTestSession() {
        if (config.isDatabaseSeedingEnabled()) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║   DATABASE SEEDING PHASE STARTED      ║");
            System.out.println("╚═══════════════════════════════════════╝\n");
            
            dataSeeder = new TestDataSeeder();
            dataSeeder.seedAllData();
            
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║   DATABASE SEEDING PHASE COMPLETED    ║");
            System.out.println("╚═══════════════════════════════════════╝\n");
        }
    }

    /**
     * Cleanup test data once after all test scenarios complete.
     * Runs at the end of the test session regardless of filter tags.
     */
    @AfterClass
    public static void teardownTestSession() {
        if (dataSeeder != null && config.isDatabaseSeedingEnabled()) {
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║   DATABASE CLEANUP PHASE STARTED      ║");
            System.out.println("╚═══════════════════════════════════════╝\n");
            
            dataSeeder.cleanupAllData();
            dataSeeder.close();
            
            System.out.println("\n╔═══════════════════════════════════════╗");
            System.out.println("║   DATABASE CLEANUP PHASE COMPLETED    ║");
            System.out.println("╚═══════════════════════════════════════╝\n");
        }
    }

    /**
     * Get the TestDataSeeder instance.
     * Useful for accessing seeded IDs in step definitions.
     */
    public static TestDataSeeder getDataSeeder() {
        return dataSeeder;
    }
}