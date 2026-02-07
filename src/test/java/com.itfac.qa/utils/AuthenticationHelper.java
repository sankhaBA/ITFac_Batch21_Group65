package com.itfac.qa.utils;

import com.itfac.qa.utils.ConfigurationManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized API Authentication Helper.
 * Provides reusable methods for API authentication and token management.
 */
public class AuthenticationHelper {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private String token;
    private String baseUri;

    public AuthenticationHelper() {
        this.baseUri = config.getApiBaseUrl();
    }

    /**
     * Authenticate user by role (Admin/Regular) and store token.
     * @param role User role (e.g., "admin", "regular", "testuser")
     * @return Authentication token
     */
    public String authenticateByRole(String role) {
        String username = config.getUsername(role);
        String password = config.getPassword(role);
        return authenticate(username, password);
    }

    /**
     * Authenticate with specific username and password.
     * @param username Username
     * @param password Password
     * @return Authentication token
     */
    public String authenticate(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        Response authResponse = RestAssured.given()
                .baseUri(baseUri)
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login");

        int statusCode = authResponse.getStatusCode();
        Assert.assertEquals("Login failed! Expected 200 but got " + statusCode, 200, statusCode);

        token = authResponse.jsonPath().getString("token");
        Assert.assertNotNull("Token was null! Login failed for user: " + username, token);
        Assert.assertFalse("Token is empty!", token.isEmpty());

        return token;
    }

    /**
     * Get stored authentication token.
     * @return Current token
     */
    public String getToken() {
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Token is not set. Please authenticate first.");
        }
        return token;
    }

    /**
     * Create authenticated RequestSpecification with token.
     * @return RequestSpecification with Authorization header
     */
    public RequestSpecification getAuthenticatedRequest() {
        return RestAssured.given()
                .baseUri(baseUri)
                .header("Authorization", "Bearer " + getToken())
                .contentType(ContentType.JSON);
    }

    /**
     * Create authenticated RequestSpecification with custom base URI.
     * @param customBaseUri Custom base URI
     * @return RequestSpecification with Authorization header
     */
    public RequestSpecification getAuthenticatedRequest(String customBaseUri) {
        return RestAssured.given()
                .baseUri(customBaseUri)
                .header("Authorization", "Bearer " + getToken())
                .contentType(ContentType.JSON);
    }

    /**
     * Set token manually (useful for sharing tokens across steps).
     * @param token Token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Clear stored token.
     */
    public void clearToken() {
        this.token = null;
    }

    /**
     * Check if user is authenticated.
     * @return true if token exists
     */
    public boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }

    /**
     * Get base URI used for authentication.
     * @return Base URI
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Set custom base URI.
     * @param baseUri Custom base URI
     */
    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}
