package com.itfac.qa.utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration manager for reading test properties.
 * Provides access to URLs, credentials, timeouts, and other test configuration.
 */
public class ConfigurationManager {

    private static ConfigurationManager instance;
    private Properties properties;
    private Dotenv dotenv;
    private static final String CONFIG_FILE = "src/test/resources/config/test.properties";

    private ConfigurationManager() {
        properties = new Properties();
        loadEnvironmentVariables();
        loadProperties();
    }

    /**
     * Get singleton instance of ConfigurationManager
     */
    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    /**
     * Load environment variables from .env file if it exists
     */
    private void loadEnvironmentVariables() {
        try {
            // Load .env file from project root
            dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
            System.out.println("Environment variables loaded from .env file");
        } catch (Exception e) {
            System.out.println("No .env file found or error loading it. Using system environment variables.");
            dotenv = null;
        }
    }

    /**
     * Load properties from configuration file
     */
    private void loadProperties() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Failed to load configuration file: " + CONFIG_FILE);
            e.printStackTrace();
            // Load default properties as fallback
            loadDefaultProperties();
        }
    }

    /**
     * Load default properties as fallback
     */
    private void loadDefaultProperties() {
        properties.setProperty("base.url", "http://localhost:8080");
        properties.setProperty("api.base.url", "http://localhost:8080");
        properties.setProperty("ui.base.url", "http://localhost:8080/ui");
        properties.setProperty("admin.username", "admin");
        properties.setProperty("admin.password", "admin123");
        properties.setProperty("regular.username", "testuser");
        properties.setProperty("regular.password", "test123");
        properties.setProperty("implicit.wait", "10");
        properties.setProperty("explicit.wait", "15");
        properties.setProperty("page.load.timeout", "30");
        properties.setProperty("browser", "chrome");
    }

    /**
     * Get property value by key
     * First checks environment variables, then falls back to properties file
     */
    public String getProperty(String key) {
        // Check environment variables first (for sensitive data)
        String envValue = getEnvironmentVariable(key);
        if (envValue != null) {
            return envValue;
        }
        // Fall back to properties file
        return properties.getProperty(key);
    }

    /**
     * Get environment variable with key mapping
     * Converts property keys to environment variable format
     * First checks .env file, then system environment variables
     */
    private String getEnvironmentVariable(String key) {
        // Convert property key to environment variable format
        // e.g., "db.url" -> "DB_URL"
        String envKey = key.toUpperCase().replace('.', '_');
        
        // Check .env file first
        if (dotenv != null) {
            String dotenvValue = dotenv.get(envKey);
            if (dotenvValue != null) {
                return dotenvValue;
            }
        }
        
        // Fall back to system environment variables
        return System.getenv(envKey);
    }

    /**
     * Get property value with default fallback
     * First checks environment variables, then properties file, then default
     */
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    // URL Configuration
    public String getBaseUrl() {
        return getProperty("base.url");
    }

    public String getApiBaseUrl() {
        return getProperty("api.base.url", getBaseUrl());
    }

    public String getUiBaseUrl() {
        return getProperty("ui.base.url", getBaseUrl() + "/ui");
    }

    // Credentials
    public String getAdminUsername() {
        return getProperty("admin.username");
    }

    public String getAdminPassword() {
        return getProperty("admin.password");
    }

    public String getRegularUsername() {
        return getProperty("regular.username");
    }

    public String getRegularPassword() {
        return getProperty("regular.password");
    }

    /**
     * Get username based on role
     */
    public String getUsername(String role) {
        return role.equalsIgnoreCase("admin") ? getAdminUsername() : getRegularUsername();
    }

    /**
     * Get password based on role
     */
    public String getPassword(String role) {
        return role.equalsIgnoreCase("admin") ? getAdminPassword() : getRegularPassword();
    }

    // Timeouts (in seconds)
    public int getImplicitWait() {
        return Integer.parseInt(getProperty("implicit.wait", "10"));
    }

    public int getExplicitWait() {
        return Integer.parseInt(getProperty("explicit.wait", "15"));
    }

    public int getPageLoadTimeout() {
        return Integer.parseInt(getProperty("page.load.timeout", "30"));
    }

    // Browser Configuration
    public String getBrowser() {
        return getProperty("browser", "chrome");
    }

    // Database Configuration
    public String getDatabaseUrl() {
        return getProperty("db.url");
    }

    public String getDatabaseUsername() {
        return getProperty("db.username");
    }

    public String getDatabasePassword() {
        return getProperty("db.password");
    }

    public String getDatabaseDriver() {
        return getProperty("db.driver");
    }

    public boolean isDatabaseSeedingEnabled() {
        return Boolean.parseBoolean(getProperty("db.seed.enabled", "true"));
    }
}
