# CampusLink Career

CampusLink Career is a Java 17 desktop application for university career management. It brings students, employers, and administrators together in one place for profiles, opportunities, applications, resources, and reporting.

## Quick start

### 1. Prerequisites

- Java 17 or newer
- Maven 3.8+ or the included Maven Wrapper
- A MySQL-compatible server (recommended) or no database setup if you want the built-in H2 fallback

### 2. Clone and run

```bash
git clone https://github.com/jox-pro/campuslink-career
cd campuslink-career
```

On Windows:

```bat
mvnw.cmd javafx:run
```

macOS/Linux:

```bash
./mvnw javafx:run
```

If you prefer a simple batch launcher on Windows, you can also run:

```bat
run.bat
```

### 3. Database setup (recommended)

If you want to use MySQL instead of the built-in fallback, start your MySQL server and import the schema:

```bash
mysql -u root -p < database/schema.sql
```

Then copy the sample configuration file and update the values:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Example:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/campuslink_career?useSSL=false&serverTimezone=UTC
db.username=root
db.password=yourpassword
db.pool.size=10
```

The app also reads the same values from environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `DB_POOL_SIZE`.

If no database configuration is supplied, the app falls back to an embedded H2 database for convenience.

### 4. Default login

A default administrator account is created on first run:

| Role  | Username | Password  |
| ----- | -------- | --------- |
| Admin | admin    | Admin@123 |

You will be asked to change this password on first login.

## Main features

- Student registration and profile management
- Job and internship browsing
- Application tracking and status updates
- Employer posting and applicant review
- Admin reporting and resource management

## Build, test, and package

```bash
./mvnw test
./mvnw package
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Project structure

```text
src/main/java/com/campuslink/
├── app/            Application entry point
├── controllers/    JavaFX UI controllers
├── dao/            Database access objects
├── models/         Data models
├── services/       Business logic
└── utils/          Database, validation, and security helpers

src/main/resources/
├── css/            Stylesheets
├── fxml/           FXML screens
├── db.properties.example  Sample database configuration
└── logback.xml     Logging configuration

database/
└── schema.sql      MySQL setup script
```

## Troubleshooting

- If Maven cannot be found, install Maven or use the provided wrapper scripts.
- If the database connection fails, check the values in `src/main/resources/db.properties`.
- If you are using XAMPP or a local MySQL setup, ensure the server is running before launching the app.

## License

MIT License — University final-year project
