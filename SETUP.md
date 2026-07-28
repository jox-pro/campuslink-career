# Setup

## Requirements
- JDK 17+ (any JDK 21+ also compiles this fine — pom.xml targets release 17 as the bytecode level)
- Maven
- MySQL-compatible server (MariaDB via XAMPP works — this is what was used to develop/verify this setup)
- Git

## 1. Database
Start MySQL/MariaDB (e.g. via XAMPP Control Panel). Then load the schema:

```
mysql -u root < database/schema.sql
```
(no `-p` flag if using default XAMPP — blank root password)

This creates the `campuslink_career` database and all 8 tables, including `audit_log`.

Verify:
```sql
USE campuslink_career;
SHOW TABLES;
```

## 2. Configure the database connection
```
copy src\main\resources\db.properties.example src\main\resources\db.properties      # Windows
cp src/main/resources/db.properties.example src/main/resources/db.properties         # macOS/Linux
```
Edit `db.properties` with real credentials. `db.properties` is gitignored — never commit it.

## 3. Build
```
mvn compile
```

## 4. Test
```
mvn test
```
Service-layer tests (`AuthServiceTest`, `StudentServiceTest`) run against mocked DAOs via Mockito — no database connection required for these to pass.

## 5. Run
```
mvn javafx:run
```

## CI
`.github/workflows/build.yml` runs `mvn compile` and `mvn test` on every push, on GitHub's runners
(full internet access, unlike some sandboxed dev environments — this is where a build actually gets
verified end-to-end for the first time). Check the Actions tab on GitHub after pushing to see real
pass/fail, not an assumption.

## What's NOT set up yet
- Connection pooling (HikariCP) — `AppDataSource` currently opens a new connection per call
- Lecturer/Staff module + academic taxonomy tables
- SMTP email notifications, CSV export
- JasperReports PDF export (dependency is present and version-pinned to 7.0.7, but not yet wired into any controller)

These are deliberately out of scope for this state of the repo — see project notes for phased sequencing.
