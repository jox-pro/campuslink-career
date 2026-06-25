# Claude Instructions for CampusLink Career

## What This Is

A **Java 17 desktop application** using JavaFX, Maven, MySQL (JDBC), and BCrypt.
Career management platform for university students, employers, and administrators.

## Architecture

MVC Pattern:
- **Models** — POJOs in `src/main/java/com/campuslink/models/`
- **Views** — FXML files in `src/main/resources/fxml/`
- **Controllers** — JavaFX controllers in `src/main/java/com/campuslink/controllers/`
- **DAOs** — Database access in `src/main/java/com/campuslink/dao/`
- **Services** — Business logic in `src/main/java/com/campuslink/services/`
- **Utils** — DBConnection, PasswordUtil, ValidationUtil, SessionManager

## Commands

```bash
mvn javafx:run      # Run application
mvn compile         # Compile only
mvn package         # Build JAR
mvn test            # Run tests
```

## Database Setup

1. Install MySQL, start the server
2. Run: `mysql -u root -p < database/schema.sql`
3. Update db.url/db.username/db.password in `src/main/resources/db.properties`

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Employer | techcorp | employer123 |

## Rules

DO:
- Use PreparedStatements everywhere — NEVER string-concatenated SQL
- Hash passwords with BCrypt (PasswordUtil)
- Check SessionManager for current user role before actions
- Load sub-views into the StackPane contentArea via FXMLLoader
- Use Platform.runLater() for UI updates from non-FX threads

DO NOT:
- Store plaintext passwords
- Use raw SQL string concatenation (SQL injection risk)
- Call UI code from non-JavaFX threads without Platform.runLater
