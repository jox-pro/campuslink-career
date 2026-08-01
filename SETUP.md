# Setup guide

## Requirements

- JDK 17 or newer
- Maven 3.8+ or the included Maven wrapper
- A running MySQL-compatible server if you want to use the main database path
- Git

## Option A: Quick start with the built-in fallback

This is the easiest path if you just want to run the app locally.

1. Clone the repository.
2. Run the launcher:
   - Windows: `mvnw.cmd javafx:run`
   - macOS/Linux: `./mvnw javafx:run`
3. The app will start with an embedded H2 database automatically.

## Option B: Full MySQL setup

### 1. Create the database

Start MySQL or MariaDB and import the schema:

```bash
mysql -u root -p < database/schema.sql
```

If you are using XAMPP with a blank root password, this usually works without `-p`.

### 2. Configure credentials

Copy the sample configuration file:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Then edit the file and enter your real login details.

Example:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/campuslink_career?useSSL=false&serverTimezone=UTC
db.username=root
db.password=yourpassword
db.pool.size=10
```

### 3. Build and test

```bash
./mvnw test
```

### 4. Run the app

```bash
./mvnw javafx:run
```

## Default credentials

On first launch, the app creates an administrator account with:

- Username: `admin`
- Password: `Admin@123`

You will be prompted to change the password after first login.

## Build and package

```bash
./mvnw package
```

This produces a runnable shaded JAR in the target directory.

## Troubleshooting

- If Maven is not found, use the wrapper scripts instead.
- If the database connection fails, verify the values in `src/main/resources/db.properties`.
- If you are on Windows, `run.bat` is a simple shortcut for launching the app.
