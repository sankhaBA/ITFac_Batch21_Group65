package com.itfac.qa.utils;

import com.itfac.qa.hooks.Hooks;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Centralized UI Login Helper.
 * Provides reusable methods for UI login operations across test scenarios.
 */
public class LoginHelper {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();

    // Common locators for login page
    private static final By USERNAME_FIELD = By.name("username");
    private static final By PASSWORD_FIELD = By.name("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By LOGIN_BUTTON_ALT = By.xpath("//button[contains(text(),'Login') or contains(text(),'Sign')]");

    /**
     * Navigate to login page.
     */
    public static void navigateToLoginPage() {
        WebDriver driver = Hooks.getDriver();
        driver.get(config.getUiBaseUrl() + "/login");
    }

    /**
     * Perform login by role (Admin/Regular).
     * @param role User role (e.g., "admin", "regular")
     */
    public static void loginByRole(String role) {
        String username = config.getUsername(role);
        String password = config.getPassword(role);
        login(username, password);
    }

    /**
     * Perform login with specific username and password.
     * @param username Username
     * @param password Password
     */
    public static void login(String username, String password) {
        WebDriver driver = Hooks.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getExplicitWait()));

        // Wait for login page to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_FIELD));

        // Enter username
        WebElement usernameField = driver.findElement(USERNAME_FIELD);
        usernameField.clear();
        usernameField.sendKeys(username);

        // Enter password
        WebElement passwordField = driver.findElement(PASSWORD_FIELD);
        passwordField.clear();
        passwordField.sendKeys(password);

        // Click login button
        clickLoginButton(driver, wait);

        // Wait for redirect to dashboard
        wait.until(ExpectedConditions.urlContains("dashboard"));
    }

    /**
     * Perform login with role, navigating to login page first.
     * @param role User role
     */
    public static void navigateAndLoginByRole(String role) {
        navigateToLoginPage();
        loginByRole(role);
    }

    /**
     * Perform login with credentials, navigating to login page first.
     * @param username Username
     * @param password Password
     */
    public static void navigateAndLogin(String username, String password) {
        navigateToLoginPage();
        login(username, password);
    }

    /**
     * Check if already logged in by checking URL.
     * @return true if URL contains dashboard or user is not on login page
     */
    public static boolean isLoggedIn() {
        WebDriver driver = Hooks.getDriver();
        String currentUrl = driver.getCurrentUrl();
        return currentUrl.contains("dashboard") || !currentUrl.contains("login");
    }

    /**
     * Perform conditional login only if not already logged in.
     * @param role User role
     */
    public static void ensureLoggedIn(String role) {
        if (!isLoggedIn()) {
            navigateToLoginPage();
            loginByRole(role);
        }
    }

    /**
     * Enter username only (useful for testing individual fields).
     * @param username Username to enter
     */
    public static void enterUsername(String username) {
        WebDriver driver = Hooks.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getExplicitWait()));
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_FIELD));
        usernameField.clear();
        usernameField.sendKeys(username);
    }

    /**
     * Enter password only (useful for testing individual fields).
     * @param password Password to enter
     */
    public static void enterPassword(String password) {
        WebDriver driver = Hooks.getDriver();
        WebElement passwordField = driver.findElement(PASSWORD_FIELD);
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    /**
     * Click login button.
     */
    public static void clickLoginButton() {
        WebDriver driver = Hooks.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getExplicitWait()));
        clickLoginButton(driver, wait);
    }

    /**
     * Internal method to click login button with fallback.
     */
    private static void clickLoginButton(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
            loginButton.click();
        } catch (Exception e) {
            // Fallback to alternative locator
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON_ALT));
            loginButton.click();
        }
    }

    /**
     * Logout from application.
     * Finds and clicks logout button/link.
     */
    public static void logout() {
        WebDriver driver = Hooks.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(config.getExplicitWait()));

        By logoutLocator = By.xpath("//*[contains(text(),'Logout')] | //*[@title='Logout'] | //a[contains(@href,'logout')]");
        
        try {
            WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(logoutLocator));
            logoutBtn.click();
            
            // Wait for redirect to login page
            wait.until(ExpectedConditions.urlContains("login"));
        } catch (Exception e) {
            System.out.println("Logout button not found or logout failed: " + e.getMessage());
        }
    }

    /**
     * Wait for dashboard to load after successful login.
     * @param timeoutSeconds Timeout in seconds
     * @return true if dashboard loaded
     */
    public static boolean waitForDashboard(int timeoutSeconds) {
        WebDriver driver = Hooks.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        
        try {
            wait.until(ExpectedConditions.urlContains("dashboard"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for dashboard with default timeout from config.
     * @return true if dashboard loaded
     */
    public static boolean waitForDashboard() {
        return waitForDashboard(config.getExplicitWait());
    }
}
