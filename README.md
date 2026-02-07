# QA Training - Test Automation Framework

Black Box Testing Suite using Cucumber BDD, Selenium WebDriver, and RestAssured.

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL Database
- Chrome/Firefox browser (for UI tests)

## Setup Instructions

### 1. Database Configuration

The project uses environment variables for database credentials to keep sensitive information secure.

#### Quick Setup:

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Edit `.env` and update with your actual database credentials:
   ```properties
   DB_URL=jdbc:mysql://localhost:3306/qa_training?useSSL=false&allowPublicKeyRetrieval=true
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   DB_DRIVER=com.mysql.cj.jdbc.Driver
   ```

3. **Important**: Never commit the `.env` file to version control. It's already in `.gitignore`.

#### How it Works:

- The `ConfigurationManager` automatically loads environment variables from the `.env` file
- Priority order: `.env file` → `system environment variables` → `test.properties` (fallback)
- Database credentials are read from environment variables (DB_URL, DB_USERNAME, DB_PASSWORD)
- Other test configurations remain in `test.properties`

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Run Tests

Run all tests:
```bash
mvn test
```

Run specific test types:
```bash
# Run only API tests
mvn test -Dcucumber.filter.tags="@API"

# Run only UI tests
mvn test -Dcucumber.filter.tags="@UI"

# Run specific feature tests
mvn test -Dcucumber.filter.tags="@Category"
```

## Project Structure

```
src/test/
├── java/com.itfac.qa/
│   ├── hooks/              # Test hooks for setup/teardown
│   ├── runners/            # Test runners
│   ├── stepDefinitions/    # Cucumber step definitions
│   │   ├── api/           # API test steps
│   │   └── ui/            # UI test steps
│   └── utils/             # Utility classes
│       ├── ConfigurationManager.java
│       ├── DatabaseHandler.java
│       └── TestDataSeeder.java
└── resources/
    ├── config/
    │   └── test.properties # Test configuration
    └── features/           # Cucumber feature files
        ├── api/
        └── ui/
```

## Data Seeding

The framework includes automatic database seeding:

- **When**: Once per test session (before all tests)
- **Cleanup**: Once after all tests complete
- **Enable/Disable**: Set `db.seed.enabled=true/false` in `test.properties`

The seeding runs automatically when you execute `mvn test`, regardless of tag filters.

## Configuration

Main configuration file: `src/test/resources/config/test.properties`

Key settings:
- Base URLs (API and UI)
- Browser configuration
- Test timeouts
- Database seeding options

## Reports

After test execution, reports are generated in:
- `target/cucumber-reports.html` - HTML report
- `target/surefire-reports/` - JUnit XML reports

## Troubleshooting

### Database Connection Issues

1. Verify MySQL is running
2. Check credentials in `.env` file
3. Ensure database exists: `CREATE DATABASE qa_training;`
4. Verify connection URL format

### Environment Variables Not Loading

1. Ensure `.env` file exists in project root
2. Check file format (no spaces around `=`)
3. Restart IDE if using integrated terminal
4. Run `mvn clean compile` to refresh

## Security Notes

- ✅ `.env` is in `.gitignore` - credentials stay local
- ✅ Use `.env.example` as template for team members
- ✅ Never commit actual credentials to repository
- ✅ Production environments should use actual environment variables, not .env files
