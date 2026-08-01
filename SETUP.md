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

**Troubleshooting Maven:** If Maven fails because of an inaccessible local repository path on your machine, ensure your user has full permissions to the `~/.m2/repository` directory or configure a different path in your global Maven `settings.xml`.

## 5. Default Credentials

A default administrator account is created by the schema:

- **Username:** admin
- **Password:** Admin@123
- **Role:** ADMIN

**Important:** You must change this password immediately after the first login. The application will force this change before allowing dashboard access.

## 6. Run

```
mvn javafx:run
```

## CI

`.github/workflows/build.yml` runs `mvn compile` and `mvn test` on every push, on GitHub's runners
(full internet access, unlike some sandboxed dev environments — this is where a build actually gets
verified end-to-end for the first time). Check the Actions tab on GitHub after pushing to see real
pass/fail, not an assumption.

## Packaging and portability

A runnable fat JAR is produced by `mvn package`, and the app uses user-home storage for uploaded CVs and resources so it behaves correctly on Windows, macOS, and Linux.

For native packaging, the project can be launched with `mvn javafx:run` or packaged into a distributable with a Java runtime (for example via jpackage once a platform-specific installer is desired).

## What's left for a larger rollout

- Lecturer/Staff module + academic taxonomy tables
- Optional SMTP email notifications for application and status updates
- Additional report templates beyond the current CSV/PDF export flow
