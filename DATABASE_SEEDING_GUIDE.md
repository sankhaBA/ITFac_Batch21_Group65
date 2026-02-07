# Database Handler & Test Data Seeding Guide

## Overview

A robust database management system has been integrated into your test automation framework. This system automatically seeds test data before tests run and cleans up after tests complete, ensuring test data isolation and consistency.

## Architecture

### Components Created

1. **DatabaseHandler.java** - Core database operations utility
2. **TestDataSeeder.java** - Test data lifecycle management
3. **Updated Hooks.java** - Integrated seeding hooks
4. **Updated ConfigurationManager.java** - Database configuration support
5. **Updated test.properties** - Database connection settings
6. **Updated pom.xml** - MySQL connector dependency

## Features

### DatabaseHandler Features

- ✅ Connection management with auto-reconnect
- ✅ Prepared statements for SQL injection prevention
- ✅ Query execution (SELECT) with result mapping
- ✅ Update execution (INSERT/UPDATE/DELETE)
- ✅ Batch operations for bulk data
- ✅ Transaction support (begin/commit/rollback)
- ✅ Helper methods for common operations
- ✅ Safe table truncation with foreign key handling

### TestDataSeeder Features

- ✅ Automatic test data generation
- ✅ Foreign key relationship handling
- ✅ ID tracking for cleanup
- ✅ Configurable seeding (enable/disable)
- ✅ Complete cleanup after tests
- ✅ Thread-safe seeding for parallel execution

## Configuration

### Database Settings (test.properties)

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/qa_training?useSSL=false&allowPublicKeyRetrieval=true
db.username=root
db.password=admin
db.driver=com.mysql.cj.jdbc.Driver

# Enable/disable database seeding
db.seed.enabled=true
```

### Customization

To disable database seeding for specific test runs:
- Set `db.seed.enabled=false` in test.properties
- Or pass as system property: `-Ddb.seed.enabled=false`

## Test Data Seeded

### Categories (10 records)
- Main categories: Flowers, Trees, Herbs, Vegetables, Fruits
- Sub-categories: Roses, Tulips, Orchids, Oak Trees, Pine Trees

### Plants (10 records)
- Red Rose, Yellow Tulip, White Orchid
- Oak Sapling, Pine Seedling
- Basil Plant, Tomato Plant, Strawberry Plant
- Lavender, Sunflower

### Inventory (10 records)
- Initial stock entries for all plants
- Sample IN/OUT transactions
- Linked to plant records

### Sales (8 records)
- Sales transactions for various plants
- Different dates (last 5 days)
- Properly calculated total prices

## Usage in Tests

### Accessing Seeded Data in Step Definitions

```java
import com.itfac.qa.hooks.Hooks;
import com.itfac.qa.utils.TestDataSeeder;
import com.itfac.qa.utils.DatabaseHandler;
import com.itfac.qa.runners.TestRunner;

public class MySteps {
    
    // Get the data seeder instance
    TestDataSeeder seeder = TestRunner.getDataSeeder();
    
    // Get seeded IDs for assertions
    List<Long> categoryIds = seeder.getSeededIds("categories");
    List<Long> plantIds = seeder.getSeededIds("plants");
    
    // Access database handler for custom queries
    DatabaseHandler dbHandler = seeder.getDbHandler();
    
    // Example: Query a specific plant
    Map<String, Object> plant = dbHandler.getRecordById("plants", plantIds.get(0));
    String plantName = (String) plant.get("name");
    Double plantPrice = (Double) plant.get("price");
    
    // Example: Custom query
    List<Map<String, Object>> results = dbHandler.executeQuery(
        "SELECT * FROM plants WHERE category_id = ?", 
        categoryIds.get(0)
    );
}
```

### Custom Database Operations

```java
// Insert a custom record
long newPlantId = dbHandler.executeInsertAndGetId(
    "INSERT INTO plants (name, price, quantity, category_id) VALUES (?, ?, ?, ?)",
    "Test Plant", 19.99, 100, categoryIds.get(0)
);

// Update a record
dbHandler.executeUpdate(
    "UPDATE plants SET quantity = ? WHERE id = ?",
    50, plantIds.get(0)
);

// Delete a record
dbHandler.executeUpdate(
    "DELETE FROM plants WHERE id = ?",
    plantIds.get(0)
);

// Check if record exists
boolean exists = dbHandler.recordExists(
    "plants", 
    "name = ?", 
    "Red Rose"
);

// Transaction example
dbHandler.beginTransaction();
try {
    dbHandler.executeUpdate("UPDATE plants SET quantity = quantity - 1 WHERE id = ?", plantId);
    dbHandler.executeInsertAndGetId("INSERT INTO sales (plant_id, quantity, total_price, sold_at) VALUES (?, ?, ?, NOW())", 
        plantId, 1, 15.99);
    dbHandler.commit();
} catch (Exception e) {
    dbHandler.rollback();
    throw e;
}
```

## Lifecycle Flow

### Test Execution Flow

```
1. @BeforeClass (TestRunner) - setupTestSession()
   ↓
   Seed all test data ONCE (categories → plants → inventory → sales)
   ↓
2. @Before("@UI") (Hooks) - setup()
   ↓
   Initialize WebDriver for UI tests (per scenario)
   ↓
3. Run test scenarios
   ↓
4. @After("@UI") (Hooks) - tearDown()
   ↓
   Close WebDriver (per scenario)
   ↓
5. @AfterClass (TestRunner) - teardownTestSession()
   ↓
   Clean up test data ONCE (sales → inventory → plants → categories)
```

### Hook Order Explanation

- **@BeforeClass**: Database seeding (runs ONCE before all tests)
- **@Before("@UI")**: UI setup per scenario (WebDriver initialization)
- **@After("@UI")**: UI teardown per scenario (WebDriver cleanup)
- **@AfterClass**: Database cleanup (runs ONCE after all tests)

## Best Practices

### 1. Don't Modify Seeded Data Directly

```java
// ❌ BAD: Modifying seeded data
dbHandler.executeUpdate("DELETE FROM categories WHERE id = ?", categoryIds.get(0));

// ✅ GOOD: Create your own test data
long testCategoryId = dbHandler.executeInsertAndGetId(
    "INSERT INTO categories (name, parent_id) VALUES (?, ?)",
    "Test Category", null
);
// Clean up in test
dbHandler.executeUpdate("DELETE FROM categories WHERE id = ?", testCategoryId);
```

### 2. Use Seeded Data for Read-Only Operations

```java
// ✅ GOOD: Using seeded data for queries
List<Long> plantIds = seeder.getSeededIds("plants");
Map<String, Object> plant = dbHandler.getRecordById("plants", plantIds.get(0));
// Assert on the data
assertEquals("Red Rose", plant.get("name"));
```

### 3. Verify Data State Before Tests

```java
// ✅ GOOD: Verify expected state
@Given("the database has test plants")
public void verifyTestData() {
    List<Long> plantIds = TestRunner.getDataSeeder().getSeededIds("plants");
    assertTrue("No test plants found", plantIds.size() > 0);
}
```

## Troubleshooting

### Issue: Connection Refused

**Solution**: Ensure MySQL server is running and credentials are correct in test.properties

```bash
# Check MySQL status
mysql -u root -p
```

### Issue: Foreign Key Constraints

**Solution**: The seeder handles foreign keys automatically. Ensure cleanup order is correct (child tables before parent tables).

### Issue: Duplicate Data

**Solution**: The seeder cleans up before seeding. If you see duplicates, check if cleanup is being called properly.

### Issue: Tests Failing After Seeding

**Solution**: Check if your tests depend on specific IDs. Use the seeded IDs from `getSeededIds()` instead of hardcoding.

## Testing the Setup

Run a simple test to verify the setup:

```bash
# Run API tests
mvn test -Dcucumber.filter.tags="@API"

# Run UI tests
mvn test -Dcucumber.filter.tags="@UI"

# Run specific feature
mvn test -Dcucumber.filter.tags="@API and @Plants"
```

### Expected Console Output

```
╔═══════════════════════════════════════╗
║   DATABASE SEEDING PHASE STARTED      ║
╚═══════════════════════════════════════╝

========================================
Starting test data seeding...
========================================
Database connection established successfully
Seeding categories...
Inserted record with ID: 1
Inserted record with ID: 2
...
Seeded 10 categories
Seeding plants...
...
Test data seeding completed successfully

╔═══════════════════════════════════════╗
║   DATABASE SEEDING PHASE COMPLETED    ║
╚═══════════════════════════════════════╝
```

## Advanced Configuration

### Disable Seeding for Specific Tests

Add a custom tag in your feature files:

```gherkin
@API @Plants @NoDbSeed
Scenario: Test without database seeding
  Given I have my own test data
  When I perform operations
  Then I verify results
```

Then update Hooks.java to check for the tag:

```java
@Before(order = 100)
public void seedTestData(Scenario scenario) {
    if (scenario.getSourceTagNames().contains("@NoDbSeed")) {
        return; // Skip seeding
    }
    // ... existing seeding logic
}
```

### Custom Seeding Strategies

Create additional seeder methods:

```java
// In TestDataSeeder.java
public void seedMinimalData() {
    // Seed only essential data
    seedCategories(); // Only main categories
}

public void seedLargeDataset() {
    // Seed large dataset for performance testing
    seedCategories();
    seedManyPlants(1000); // Custom method to seed 1000 plants
}
```

## Summary

The database seeding system provides:

1. ✅ **Automated data management** - No manual database setup
2. ✅ **Test isolation** - Each test run has fresh data
3. ✅ **Consistency** - Same data structure every time
4. ✅ **Flexibility** - Easy to customize and extend
5. ✅ **Thread-safe** - Supports parallel execution
6. ✅ **Clean state** - Automatic cleanup after tests

**Minimal Code Changes**:
- Only 1 file change required (added hooks in Hooks.java)
- 2 new utility classes (DatabaseHandler, TestDataSeeder)
- 1 dependency added (MySQL connector)
- Configuration updates (test.properties, ConfigurationManager)

Your existing test code remains unchanged and will now benefit from automatic database seeding! 🎉
