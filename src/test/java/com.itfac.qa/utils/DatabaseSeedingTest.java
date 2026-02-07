package com.itfac.qa.utils;

import java.util.List;
import java.util.Map;

/**
 * Standalone utility to test database connection and seeding.
 * Run this class directly to verify database setup without running full test suite.
 * 
 * Usage:
 * - From IDE: Right-click and select "Run" or "Debug"
 * - From Maven: mvn exec:java -Dexec.mainClass="com.itfac.qa.utils.DatabaseSeedingTest"
 */
public class DatabaseSeedingTest {

    public static void main(String[] args) {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║   DATABASE CONNECTION & SEEDING TEST                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        TestDataSeeder seeder = null;
        DatabaseHandler dbHandler = null;

        try {
            // Step 1: Test database connection
            System.out.println("Step 1: Testing database connection...");
            dbHandler = new DatabaseHandler();
            
            if (dbHandler.isConnectionValid()) {
                System.out.println("✅ Database connection successful!\n");
            } else {
                System.err.println("❌ Database connection failed!\n");
                return;
            }

            // Step 2: Test seeding
            System.out.println("Step 2: Testing data seeding...");
            seeder = new TestDataSeeder();
            seeder.seedAllData();
            System.out.println("✅ Data seeding successful!\n");

            // Step 3: Verify seeded data
            System.out.println("Step 3: Verifying seeded data...");
            verifySeededData(seeder);
            System.out.println("✅ Data verification successful!\n");

            // Step 4: Display seeded data summary
            System.out.println("Step 4: Seeded data summary:");
            displayDataSummary(seeder);

            // Step 5: Ask user if they want to keep or cleanup data
            System.out.println("\n════════════════════════════════════════════════════════");
            System.out.println("Database seeding test completed successfully!");
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("\nOptions:");
            System.out.println("1. Keep data in database (comment out cleanup)");
            System.out.println("2. Clean up data (default behavior)");
            System.out.println("\nCurrent behavior: Cleaning up test data...\n");

            // Step 6: Cleanup (comment this out if you want to keep the data)
            if (seeder != null) {
                seeder.cleanupAllData();
                System.out.println("✅ Cleanup successful!\n");
            }

            System.out.println("╔═══════════════════════════════════════════════════════╗");
            System.out.println("║   TEST COMPLETED SUCCESSFULLY                         ║");
            System.out.println("╚═══════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR: Database seeding test failed!");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("\nPossible issues:");
            System.err.println("1. MySQL server is not running");
            System.err.println("2. Database 'qa_training' does not exist");
            System.err.println("3. Incorrect credentials in test.properties");
            System.err.println("4. Tables do not exist in the database");
            System.err.println("5. MySQL JDBC driver not found\n");
            
            e.printStackTrace();
            
        } finally {
            // Close connections
            if (seeder != null) {
                seeder.close();
            }
            if (dbHandler != null) {
                dbHandler.close();
            }
        }
    }

    /**
     * Verify that data was seeded correctly
     */
    private static void verifySeededData(TestDataSeeder seeder) {
        DatabaseHandler dbHandler = seeder.getDbHandler();

        // Verify categories
        List<Long> categoryIds = seeder.getSeededIds("categories");
        if (categoryIds.isEmpty()) {
            throw new RuntimeException("No categories were seeded!");
        }
        System.out.println("  ✓ Categories seeded: " + categoryIds.size());

        // Verify plants
        List<Long> plantIds = seeder.getSeededIds("plants");
        if (plantIds.isEmpty()) {
            throw new RuntimeException("No plants were seeded!");
        }
        System.out.println("  ✓ Plants seeded: " + plantIds.size());

        // Verify inventory
        List<Long> inventoryIds = seeder.getSeededIds("inventory");
        if (inventoryIds.isEmpty()) {
            throw new RuntimeException("No inventory records were seeded!");
        }
        System.out.println("  ✓ Inventory records seeded: " + inventoryIds.size());

        // Verify sales
        List<Long> salesIds = seeder.getSeededIds("sales");
        if (salesIds.isEmpty()) {
            throw new RuntimeException("No sales records were seeded!");
        }
        System.out.println("  ✓ Sales records seeded: " + salesIds.size());

        // Verify database counts match
        List<Map<String, Object>> categoryCount = dbHandler.executeQuery(
            "SELECT COUNT(*) as count FROM categories WHERE id IN (" + 
            String.join(",", categoryIds.stream().map(String::valueOf).toArray(String[]::new)) + ")"
        );
        long dbCategoryCount = ((Number) categoryCount.get(0).get("count")).longValue();
        if (dbCategoryCount != categoryIds.size()) {
            throw new RuntimeException("Category count mismatch! Expected: " + categoryIds.size() + ", Found: " + dbCategoryCount);
        }
    }

    /**
     * Display summary of seeded data with sample records
     */
    private static void displayDataSummary(TestDataSeeder seeder) {
        DatabaseHandler dbHandler = seeder.getDbHandler();

        // Display categories
        System.out.println("\n  📁 Categories:");
        List<Long> categoryIds = seeder.getSeededIds("categories");
        List<Map<String, Object>> categories = dbHandler.executeQuery(
            "SELECT id, name, parent_id FROM categories WHERE id IN (" + 
            String.join(",", categoryIds.stream().map(String::valueOf).toArray(String[]::new)) + ") LIMIT 5"
        );
        for (Map<String, Object> cat : categories) {
            System.out.println("     - ID: " + cat.get("id") + ", Name: " + cat.get("name") + 
                             ", Parent: " + (cat.get("parent_id") != null ? cat.get("parent_id") : "None"));
        }
        if (categoryIds.size() > 5) {
            System.out.println("     ... and " + (categoryIds.size() - 5) + " more");
        }

        // Display plants
        System.out.println("\n  🌱 Plants:");
        List<Long> plantIds = seeder.getSeededIds("plants");
        List<Map<String, Object>> plants = dbHandler.executeQuery(
            "SELECT id, name, price, quantity FROM plants WHERE id IN (" + 
            String.join(",", plantIds.stream().map(String::valueOf).toArray(String[]::new)) + ") LIMIT 5"
        );
        for (Map<String, Object> plant : plants) {
            System.out.println("     - ID: " + plant.get("id") + ", Name: " + plant.get("name") + 
                             ", Price: $" + plant.get("price") + ", Qty: " + plant.get("quantity"));
        }
        if (plantIds.size() > 5) {
            System.out.println("     ... and " + (plantIds.size() - 5) + " more");
        }

        // Display inventory summary
        System.out.println("\n  📦 Inventory:");
        List<Long> inventoryIds = seeder.getSeededIds("inventory");
        List<Map<String, Object>> inventory = dbHandler.executeQuery(
            "SELECT i.id, i.type, i.quantity, p.name as plant_name FROM inventory i " +
            "JOIN plants p ON i.plant_id = p.id WHERE i.id IN (" + 
            String.join(",", inventoryIds.stream().map(String::valueOf).toArray(String[]::new)) + ") LIMIT 5"
        );
        for (Map<String, Object> inv : inventory) {
            System.out.println("     - ID: " + inv.get("id") + ", Type: " + inv.get("type") + 
                             ", Qty: " + inv.get("quantity") + ", Plant: " + inv.get("plant_name"));
        }
        if (inventoryIds.size() > 5) {
            System.out.println("     ... and " + (inventoryIds.size() - 5) + " more");
        }

        // Display sales summary
        System.out.println("\n  💰 Sales:");
        List<Long> salesIds = seeder.getSeededIds("sales");
        List<Map<String, Object>> sales = dbHandler.executeQuery(
            "SELECT s.id, s.quantity, s.total_price, p.name as plant_name FROM sales s " +
            "JOIN plants p ON s.plant_id = p.id WHERE s.id IN (" + 
            String.join(",", salesIds.stream().map(String::valueOf).toArray(String[]::new)) + ") LIMIT 5"
        );
        for (Map<String, Object> sale : sales) {
            System.out.println("     - ID: " + sale.get("id") + ", Plant: " + sale.get("plant_name") + 
                             ", Qty: " + sale.get("quantity") + ", Total: $" + sale.get("total_price"));
        }
        if (salesIds.size() > 5) {
            System.out.println("     ... and " + (salesIds.size() - 5) + " more");
        }

        System.out.println("\n  Total records seeded: " + 
            (categoryIds.size() + plantIds.size() + inventoryIds.size() + salesIds.size()));
    }
}
