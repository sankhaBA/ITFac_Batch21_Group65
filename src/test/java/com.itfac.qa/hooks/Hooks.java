package com.itfac.qa.hooks;

import com.itfac.qa.utils.ConfigurationManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

/**
 * Cucumber Hooks for test setup and teardown.
 * Uses ThreadLocal pattern for WebDriver to support parallel test execution.
 */
public class Hooks {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    /**
     * Get the WebDriver instance for the current thread.
     * This method should be used instead of accessing driver field directly.
     */
    public static WebDriver getDriver() {
        return driver.get();
    }

    /**
     * Set up WebDriver before each UI test scenario.
     * Creates browser instance based on configuration.
     */
    @Before("@UI") 
    public void setup() {
        WebDriver webDriver = createDriver();
        driver.set(webDriver);
        
        // Configure timeouts
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getImplicitWait()));
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.getPageLoadTimeout()));
        webDriver.manage().window().maximize();
    }

    /**
     * Clean up WebDriver after each UI test scenario.
     */
    @After("@UI")
    public void tearDown() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            webDriver.quit();
            driver.remove(); // Clean up ThreadLocal to prevent memory leaks
        }
    }

    /**
     * Create WebDriver instance based on browser configuration.
     * Supports: chrome, firefox, edge
     */
    private WebDriver createDriver() {
        String browser = config.getBrowser().toLowerCase();
        
        switch (browser) {
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--remote-allow-origins=*");
                return new FirefoxDriver(firefoxOptions);
                
            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--remote-allow-origins=*");
                return new EdgeDriver(edgeOptions);
                
            case "chrome":
            default:
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                return new ChromeDriver(chromeOptions);
        }
    }
}