package com.itfac.qa.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Hooks {

    public static WebDriver driver;

    @Before("@UI") 
    public void setup() {
        // Setup Chrome options (headless matches some CI environments, but keep valid for now)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @After("@UI")
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}