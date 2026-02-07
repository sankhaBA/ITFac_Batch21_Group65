package com.itfac.qa.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Test data seeder for database operations.
 * Seeds test data before tests and cleans up after tests.
 */
public class TestDataSeeder {

    private final DatabaseHandler dbHandler;
    private final Map<String, List<Long>> seededIds;
    private static final ConfigurationManager config = ConfigurationManager.getInstance();

    /**
     * Constructor initializes database handler and tracking map
     */
    public TestDataSeeder() {
        this.dbHandler = new DatabaseHandler();
        this.seededIds = new HashMap<>();
        initializeTracking();
    }

    /**
     * Initialize tracking for all tables
     */
    private void initializeTracking() {
        seededIds.put("categories", new ArrayList<>());
        seededIds.put("plants", new ArrayList<>());
        seededIds.put("inventory", new ArrayList<>());
        seededIds.put("sales", new ArrayList<>());
    }

    /**
     * Seed all test data for the testing phase
     */
    public void seedAllData() {
        if (!config.isDatabaseSeedingEnabled()) {
            System.out.println("Database seeding is disabled in configuration");
            return;
        }

        System.out.println("========================================");
        System.out.println("Starting test data seeding...");
        System.out.println("========================================");

        try {
            cleanupAllData(); // Clean existing test data first
            
            // Seed data in order (respecting foreign key constraints)
            seedCategories();
            seedPlants();
            seedInventory();
            seedSales();
            
            System.out.println("========================================");
            System.out.println("Test data seeding completed successfully");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("Failed to seed test data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Test data seeding failed", e);
        }
    }

    /**
     * Seed categories table with test data
     */
    public void seedCategories() {
        System.out.println("Seeding categories...");
        
        List<Map<String, Object>> categories = Arrays.asList(
            createCategory("Flowers", null),
            createCategory("Trees", null),
            createCategory("Herbs", null),
            createCategory("Vegetables", null),
            createCategory("Fruits", null)
        );

        for (Map<String, Object> category : categories) {
            long id = insertCategory(
                (String) category.get("name"),
                (Long) category.get("parent_id")
            );
            seededIds.get("categories").add(id);
        }

        // Add some sub-categories
        Long flowersId = seededIds.get("categories").get(0);
        Long treesId = seededIds.get("categories").get(1);
        
        long roseId = insertCategory("Roses", flowersId);
        long tulipId = insertCategory("Tulips", flowersId);
        long orchidId = insertCategory("Orchids", flowersId);
        long oakId = insertCategory("Oak Trees", treesId);
        long pineId = insertCategory("Pine Trees", treesId);
        
        seededIds.get("categories").addAll(Arrays.asList(roseId, tulipId, orchidId, oakId, pineId));
        
        System.out.println("Seeded " + seededIds.get("categories").size() + " categories");
    }

    /**
     * Seed plants table with test data
     */
    public void seedPlants() {
        System.out.println("Seeding plants...");
        
        List<Long> categoryIds = seededIds.get("categories");
        
        List<Map<String, Object>> plants = Arrays.asList(
            createPlant("Red Rose", 15.99, 50, categoryIds.get(5)),  // Roses sub-category
            createPlant("Yellow Tulip", 12.50, 75, categoryIds.get(6)), // Tulips
            createPlant("White Orchid", 35.00, 30, categoryIds.get(7)), // Orchids
            createPlant("Oak Sapling", 45.00, 20, categoryIds.get(8)), // Oak Trees
            createPlant("Pine Seedling", 25.00, 40, categoryIds.get(9)), // Pine Trees
            createPlant("Basil Plant", 8.50, 100, categoryIds.get(2)), // Herbs
            createPlant("Tomato Plant", 10.00, 80, categoryIds.get(3)), // Vegetables
            createPlant("Strawberry Plant", 12.00, 60, categoryIds.get(4)), // Fruits
            createPlant("Lavender", 14.00, 55, categoryIds.get(2)), // Herbs
            createPlant("Sunflower", 9.99, 90, categoryIds.get(0)) // Flowers
        );

        for (Map<String, Object> plant : plants) {
            long id = insertPlant(
                (String) plant.get("name"),
                (Double) plant.get("price"),
                (Integer) plant.get("quantity"),
                (Long) plant.get("category_id")
            );
            seededIds.get("plants").add(id);
        }
        
        System.out.println("Seeded " + seededIds.get("plants").size() + " plants");
    }

    /**
     * Seed inventory table with test data
     */
    public void seedInventory() {
        System.out.println("Seeding inventory...");
        
        List<Long> plantIds = seededIds.get("plants");
        
        List<Map<String, Object>> inventories = Arrays.asList(
            createInventory(plantIds.get(0), 50, "IN", "Initial stock - Red Rose"),
            createInventory(plantIds.get(1), 75, "IN", "Initial stock - Yellow Tulip"),
            createInventory(plantIds.get(2), 30, "IN", "Initial stock - White Orchid"),
            createInventory(plantIds.get(3), 20, "IN", "Initial stock - Oak Sapling"),
            createInventory(plantIds.get(4), 40, "IN", "Initial stock - Pine Seedling"),
            createInventory(plantIds.get(0), 5, "OUT", "Sold 5 units - Red Rose"),
            createInventory(plantIds.get(1), 10, "OUT", "Sold 10 units - Yellow Tulip"),
            createInventory(plantIds.get(0), 20, "IN", "Restocked - Red Rose"),
            createInventory(plantIds.get(5), 100, "IN", "Initial stock - Basil Plant"),
            createInventory(plantIds.get(6), 80, "IN", "Initial stock - Tomato Plant")
        );

        for (Map<String, Object> inventory : inventories) {
            long id = insertInventory(
                (Long) inventory.get("plant_id"),
                (Integer) inventory.get("quantity"),
                (String) inventory.get("type"),
                (String) inventory.get("note")
            );
            seededIds.get("inventory").add(id);
        }
        
        System.out.println("Seeded " + seededIds.get("inventory").size() + " inventory records");
    }

    /**
     * Seed sales table with test data
     */
    public void seedSales() {
        System.out.println("Seeding sales...");
        
        List<Long> plantIds = seededIds.get("plants");
        
        List<Map<String, Object>> sales = Arrays.asList(
            createSale(plantIds.get(0), 5, 15.99, daysAgo(5)),  // Red Rose
            createSale(plantIds.get(1), 10, 12.50, daysAgo(4)), // Yellow Tulip
            createSale(plantIds.get(2), 3, 35.00, daysAgo(3)),  // White Orchid
            createSale(plantIds.get(0), 7, 15.99, daysAgo(2)),  // Red Rose
            createSale(plantIds.get(3), 2, 45.00, daysAgo(1)),  // Oak Sapling
            createSale(plantIds.get(4), 5, 25.00, daysAgo(1)),  // Pine Seedling
            createSale(plantIds.get(5), 15, 8.50, daysAgo(0)),  // Basil Plant
            createSale(plantIds.get(1), 8, 12.50, daysAgo(0))   // Yellow Tulip
        );

        for (Map<String, Object> sale : sales) {
            long id = insertSale(
                (Long) sale.get("plant_id"),
                (Integer) sale.get("quantity"),
                (Double) sale.get("price"),
                (Timestamp) sale.get("sold_at")
            );
            seededIds.get("sales").add(id);
        }
        
        System.out.println("Seeded " + seededIds.get("sales").size() + " sales records");
    }

    // ===========================
    // Insert Helper Methods
    // ===========================

    private long insertCategory(String name, Long parentId) {
        String query = "INSERT INTO categories (name, parent_id) VALUES (?, ?)";
        return dbHandler.executeInsertAndGetId(query, name, parentId);
    }

    private long insertPlant(String name, Double price, Integer quantity, Long categoryId) {
        String query = "INSERT INTO plants (name, price, quantity, category_id) VALUES (?, ?, ?, ?)";
        return dbHandler.executeInsertAndGetId(query, name, price, quantity, categoryId);
    }

    private long insertInventory(Long plantId, Integer quantity, String type, String note) {
        String query = "INSERT INTO inventory (plant_id, quantity, type, note, created_at) VALUES (?, ?, ?, ?, NOW())";
        return dbHandler.executeInsertAndGetId(query, plantId, quantity, type, note);
    }

    private long insertSale(Long plantId, Integer quantity, Double price, Timestamp soldAt) {
        Double totalPrice = quantity * price;
        String query = "INSERT INTO sales (plant_id, quantity, total_price, sold_at) VALUES (?, ?, ?, ?)";
        return dbHandler.executeInsertAndGetId(query, plantId, quantity, totalPrice, soldAt);
    }

    // ===========================
    // Data Creation Helper Methods
    // ===========================

    private Map<String, Object> createCategory(String name, Long parentId) {
        Map<String, Object> category = new HashMap<>();
        category.put("name", name);
        category.put("parent_id", parentId);
        return category;
    }

    private Map<String, Object> createPlant(String name, Double price, Integer quantity, Long categoryId) {
        Map<String, Object> plant = new HashMap<>();
        plant.put("name", name);
        plant.put("price", price);
        plant.put("quantity", quantity);
        plant.put("category_id", categoryId);
        return plant;
    }

    private Map<String, Object> createInventory(Long plantId, Integer quantity, String type, String note) {
        Map<String, Object> inventory = new HashMap<>();
        inventory.put("plant_id", plantId);
        inventory.put("quantity", quantity);
        inventory.put("type", type);
        inventory.put("note", note);
        return inventory;
    }

    private Map<String, Object> createSale(Long plantId, Integer quantity, Double price, Timestamp soldAt) {
        Map<String, Object> sale = new HashMap<>();
        sale.put("plant_id", plantId);
        sale.put("quantity", quantity);
        sale.put("price", price);
        sale.put("sold_at", soldAt);
        return sale;
    }

    private Timestamp daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        return new Timestamp(cal.getTimeInMillis());
    }

    // ===========================
    // Cleanup Methods
    // ===========================

    /**
     * Clean up all seeded test data
     */
    public void cleanupAllData() {
        System.out.println("========================================");
        System.out.println("Cleaning up test data...");
        System.out.println("========================================");

        try {
            // Delete in reverse order (respecting foreign key constraints)
            cleanupSales();
            cleanupInventory();
            cleanupPlants();
            cleanupCategories();
            
            // Clear tracking
            initializeTracking();
            
            System.out.println("========================================");
            System.out.println("Test data cleanup completed successfully");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("Failed to cleanup test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clean up sales data
     */
    private void cleanupSales() {
        List<Long> ids = seededIds.get("sales");
        if (!ids.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
            String query = "DELETE FROM sales WHERE id IN (" + placeholders + ")";
            dbHandler.executeUpdate(query, ids.toArray());
            System.out.println("Cleaned up " + ids.size() + " sales records");
        }
    }

    /**
     * Clean up inventory data
     */
    private void cleanupInventory() {
        List<Long> ids = seededIds.get("inventory");
        if (!ids.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
            String query = "DELETE FROM inventory WHERE id IN (" + placeholders + ")";
            dbHandler.executeUpdate(query, ids.toArray());
            System.out.println("Cleaned up " + ids.size() + " inventory records");
        }
    }

    /**
     * Clean up plants data
     */
    private void cleanupPlants() {
        List<Long> ids = seededIds.get("plants");
        if (!ids.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
            String query = "DELETE FROM plants WHERE id IN (" + placeholders + ")";
            dbHandler.executeUpdate(query, ids.toArray());
            System.out.println("Cleaned up " + ids.size() + " plants records");
        }
    }

    /**
     * Clean up categories data
     */
    private void cleanupCategories() {
        List<Long> ids = seededIds.get("categories");
        if (!ids.isEmpty()) {
            // Delete in reverse order to handle parent_id foreign keys
            Collections.reverse(ids);
            for (Long id : ids) {
                String query = "DELETE FROM categories WHERE id = ?";
                dbHandler.executeUpdate(query, id);
            }
            System.out.println("Cleaned up " + ids.size() + " categories records");
        }
    }

    /**
     * Get seeded IDs for a specific table (useful for tests)
     */
    public List<Long> getSeededIds(String tableName) {
        return new ArrayList<>(seededIds.getOrDefault(tableName, new ArrayList<>()));
    }

    /**
     * Get the database handler instance (useful for custom queries in tests)
     */
    public DatabaseHandler getDbHandler() {
        return dbHandler;
    }

    /**
     * Close database connection
     */
    public void close() {
        if (dbHandler != null) {
            dbHandler.close();
        }
    }
}
