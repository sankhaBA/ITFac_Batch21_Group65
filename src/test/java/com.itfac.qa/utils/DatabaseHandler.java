package com.itfac.qa.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Robust database handler for MySQL operations.
 * Provides connection management, query execution, and CRUD operations.
 */
public class DatabaseHandler {

    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    private Connection connection;

    /**
     * Default constructor - uses configuration from test.properties
     */
    public DatabaseHandler() {
        this(config.getDatabaseUrl(), config.getDatabaseUsername(), config.getDatabasePassword());
    }

    /**
     * Constructor with custom connection parameters
     */
    public DatabaseHandler(String url, String username, String password) {
        try {
            Class.forName(config.getDatabaseDriver());
            this.connection = DriverManager.getConnection(url, username, password);
            this.connection.setAutoCommit(true);
            System.out.println("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Database driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    /**
     * Get the current database connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Reconnect if connection is closed
                connection = DriverManager.getConnection(
                    config.getDatabaseUrl(),
                    config.getDatabaseUsername(),
                    config.getDatabasePassword()
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to validate/reconnect database: " + e.getMessage());
            throw new RuntimeException("Database connection error", e);
        }
        return connection;
    }

    /**
     * Execute a SELECT query and return results as list of maps
     */
    public List<Map<String, Object>> executeQuery(String query, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (PreparedStatement statement = prepareStatement(query, params);
             ResultSet resultSet = statement.executeQuery()) {
            
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
        } catch (SQLException e) {
            System.err.println("Query execution failed: " + e.getMessage());
            System.err.println("Query: " + query);
            throw new RuntimeException("Failed to execute query", e);
        }
        
        return results;
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE statement
     * Returns the number of affected rows
     */
    public int executeUpdate(String query, Object... params) {
        try (PreparedStatement statement = prepareStatement(query, params)) {
            int affectedRows = statement.executeUpdate();
            System.out.println("Executed update: " + affectedRows + " row(s) affected");
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Update execution failed: " + e.getMessage());
            System.err.println("Query: " + query);
            throw new RuntimeException("Failed to execute update", e);
        }
    }

    /**
     * Execute an INSERT and return the generated key (auto-increment ID)
     */
    public long executeInsertAndGetId(String query, Object... params) {
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(statement, params);
            statement.executeUpdate();
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    System.out.println("Inserted record with ID: " + id);
                    return id;
                } else {
                    throw new SQLException("Insert failed, no ID obtained");
                }
            }
        } catch (SQLException e) {
            System.err.println("Insert execution failed: " + e.getMessage());
            System.err.println("Query: " + query);
            throw new RuntimeException("Failed to execute insert", e);
        }
    }

    /**
     * Execute a batch of SQL statements (useful for bulk operations)
     */
    public int[] executeBatch(List<String> queries) {
        try (Statement statement = connection.createStatement()) {
            for (String query : queries) {
                statement.addBatch(query);
            }
            int[] results = statement.executeBatch();
            System.out.println("Executed batch of " + queries.size() + " statements");
            return results;
        } catch (SQLException e) {
            System.err.println("Batch execution failed: " + e.getMessage());
            throw new RuntimeException("Failed to execute batch", e);
        }
    }

    /**
     * Delete all records from specified tables (useful for cleanup)
     */
    public void truncateTables(String... tableNames) {
        try {
            // Disable foreign key checks
            executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            
            for (String tableName : tableNames) {
                executeUpdate("TRUNCATE TABLE " + tableName);
                System.out.println("Truncated table: " + tableName);
            }
            
            // Re-enable foreign key checks
            executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            
        } catch (Exception e) {
            System.err.println("Failed to truncate tables: " + e.getMessage());
            throw new RuntimeException("Truncate operation failed", e);
        }
    }

    /**
     * Delete records with WHERE clause (safer than truncate)
     */
    public void deleteRecords(String tableName, String whereClause, Object... params) {
        String query = "DELETE FROM " + tableName + " WHERE " + whereClause;
        executeUpdate(query, params);
    }

    /**
     * Check if a record exists
     */
    public boolean recordExists(String tableName, String whereClause, Object... params) {
        String query = "SELECT COUNT(*) as count FROM " + tableName + " WHERE " + whereClause;
        List<Map<String, Object>> results = executeQuery(query, params);
        if (!results.isEmpty()) {
            long count = ((Number) results.get(0).get("count")).longValue();
            return count > 0;
        }
        return false;
    }

    /**
     * Get a single record by ID
     */
    public Map<String, Object> getRecordById(String tableName, long id) {
        String query = "SELECT * FROM " + tableName + " WHERE id = ?";
        List<Map<String, Object>> results = executeQuery(query, id);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Helper method to prepare statement with parameters
     */
    private PreparedStatement prepareStatement(String query, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        setParameters(statement, params);
        return statement;
    }

    /**
     * Helper method to set parameters in prepared statement
     */
    private void setParameters(PreparedStatement statement, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
    }

    /**
     * Begin a transaction
     */
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
            System.out.println("Transaction started");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    /**
     * Commit the current transaction
     */
    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("Transaction committed");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }

    /**
     * Rollback the current transaction
     */
    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            System.out.println("Transaction rolled back");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }

    /**
     * Close the database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Check if connection is valid
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
