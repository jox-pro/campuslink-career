package com.campuslink.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Minimal {@link DataSource} implementation backed by {@link DriverManager}.
 *
 * <p>This exists purely as a stable seam: every DAO depends on {@code javax.sql.DataSource},
 * not on this class or on DriverManager directly. When connection pooling is introduced
 * (Phase 2), a {@code HikariDataSource} can be substituted wherever {@link #getInstance()}
 * is used today, and no DAO or service code needs to change.
 *
 * <p>Each call to {@link #getConnection()} opens a brand-new physical connection — there is
 * no caching or reuse here, unlike the old {@code DBConnection} singleton. Callers (the DAOs)
 * are responsible for closing what they open, and they do, via try-with-resources. This is
 * intentionally simple and not meant to be efficient; pooling is Phase 2's job, not this
 * class's.
 */
public class AppDataSource implements DataSource {

    private static volatile AppDataSource instance;

    private final HikariDataSource hikariDataSource;
    private final String url;

    private AppDataSource(String url, String username, String password, String driverClassName) {
        this(url, username, password, driverClassName, null);
    }

    private AppDataSource(String url, String username, String password, String driverClassName, Integer poolSize) {
        this.url = url;
        if (driverClassName != null && !driverClassName.isBlank()) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("JDBC driver not found on classpath: " + driverClassName, e);
            }
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        if (driverClassName != null && !driverClassName.isBlank()) {
            config.setDriverClassName(driverClassName);
        }
        if (poolSize != null) {
            config.setMaximumPoolSize(poolSize);
        } else {
            config.setMaximumPoolSize(10);
        }
        this.hikariDataSource = new HikariDataSource(config);
        // If using embedded H2, ensure the minimal schema exists so the app can start
        if (this.url != null && this.url.startsWith("jdbc:h2:")) {
            try (Connection c = hikariDataSource.getConnection()) {
                System.err.println("AppDataSource H2 init URL=" + this.url);

                String[] createStatements = new String[] {
                    "CREATE TABLE IF NOT EXISTS users (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "role VARCHAR(20) NOT NULL, " +
                        "must_change_password BOOLEAN DEFAULT TRUE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS students (" +
                        "student_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id INT NOT NULL, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "phone VARCHAR(20), " +
                        "course VARCHAR(100), " +
                        "year_of_study INT, " +
                        "skills CLOB, " +
                        "cv_path VARCHAR(255), " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS employers (" +
                        "employer_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id INT NOT NULL, " +
                        "company_name VARCHAR(100) NOT NULL, " +
                        "contact_person VARCHAR(100), " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "phone VARCHAR(20), " +
                        "address VARCHAR(255), " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS jobs (" +
                        "job_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "title VARCHAR(100) NOT NULL, " +
                        "description CLOB, " +
                        "requirements CLOB, " +
                        "deadline DATE, " +
                        "employer_id INT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE SET NULL" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS internships (" +
                        "internship_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "title VARCHAR(100) NOT NULL, " +
                        "description CLOB, " +
                        "requirements CLOB, " +
                        "deadline DATE, " +
                        "employer_id INT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE SET NULL" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS applications (" +
                        "application_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "student_id INT NOT NULL, " +
                        "opportunity_type VARCHAR(20) NOT NULL, " +
                        "opportunity_id INT NOT NULL, " +
                        "application_date DATE NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'PENDING', " +
                        "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS resources (" +
                        "resource_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "title VARCHAR(100) UNIQUE NOT NULL, " +
                        "description CLOB, " +
                        "file_path VARCHAR(255), " +
                        "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",
                    "CREATE TABLE IF NOT EXISTS audit_log (" +
                        "audit_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "event VARCHAR(50) NOT NULL, " +
                        "username VARCHAR(50), " +
                        "outcome VARCHAR(20) NOT NULL, " +
                        "details VARCHAR(255)" +
                        ")"
                };

                for (String stmt : createStatements) {
                    try (Statement exec = c.createStatement()) {
                        exec.execute(stmt);
                    } catch (SQLException ex) {
                        System.err.println("H2 create failed: " + ex.getMessage() + " [" + stmt.replaceAll("\\s+", " ").trim() + "]");
                    }
                }

                String[] indexStatements = new String[] {
                    "CREATE INDEX IF NOT EXISTS idx_students_user_id ON students(user_id)",
                    "CREATE INDEX IF NOT EXISTS idx_employers_user_id ON employers(user_id)",
                    "CREATE INDEX IF NOT EXISTS idx_jobs_employer_id ON jobs(employer_id)",
                    "CREATE INDEX IF NOT EXISTS idx_jobs_deadline ON jobs(deadline)",
                    "CREATE INDEX IF NOT EXISTS idx_internships_employer_id ON internships(employer_id)",
                    "CREATE INDEX IF NOT EXISTS idx_internships_deadline ON internships(deadline)",
                    "CREATE INDEX IF NOT EXISTS idx_applications_student_id ON applications(student_id)",
                    "CREATE INDEX IF NOT EXISTS idx_applications_opportunity ON applications(opportunity_id, opportunity_type)",
                    "CREATE INDEX IF NOT EXISTS idx_audit_log_username ON audit_log(username)",
                    "CREATE INDEX IF NOT EXISTS idx_audit_log_time ON audit_log(event_time)"
                };

                for (String stmt : indexStatements) {
                    try (Statement exec = c.createStatement()) {
                        exec.execute(stmt);
                    } catch (SQLException ex) {
                        System.err.println("H2 index create failed: " + ex.getMessage() + " [" + stmt.replaceAll("\\s+", " ").trim() + "]");
                    }
                }

                // Seed default admin if not present
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
                    ps.setString(1, "admin");
                    try (ResultSet rs = ps.executeQuery()) {
                        boolean needInsert = true;
                        if (rs.next()) {
                            needInsert = rs.getInt(1) == 0;
                        }
                        if (needInsert) {
                            try (PreparedStatement ins = c.prepareStatement(
                                "INSERT INTO users (username, password, role, must_change_password) VALUES (?, ?, ?, ?)") ) {
                                ins.setString(1, "admin");
                                // Pre-hashed bcrypt password matching schema.sql: Admin@123
                                ins.setString(2, "$2a$10$QfOqlbdtY0RYankmYG2SxOIaCp5zVqrTiSLUAdu2cMouWMBPaDZye");
                                ins.setString(3, "ADMIN");
                                ins.setBoolean(4, true);
                                ins.executeUpdate();
                            }
                        }
                    }
                }

                System.err.println("Embedded H2 schema loader created tables for URL=" + this.url);
                try (ResultSet rs = c.getMetaData().getTables(null, null, "%", new String[] {"TABLE"})) {
                    StringBuilder tableNames = new StringBuilder();
                    while (rs.next()) {
                        if (tableNames.length() > 0) {
                            tableNames.append(", ");
                        }
                        tableNames.append(rs.getString("TABLE_NAME"));
                    }
                    System.err.println("Embedded H2 tables: " + tableNames);
                }
            } catch (SQLException e) {
                System.err.println("Failed to initialize embedded H2 schema: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        if (hikariDataSource != null && !hikariDataSource.isClosed()) {
            hikariDataSource.close();
        }
    }

    public void close() {
        shutdown();
    }

    /**
     * Returns the process-wide {@code AppDataSource}, built from {@code db.properties} on
     * the classpath the first time it's requested.
     */
    public static AppDataSource getInstance() {
        AppDataSource local = instance;
        if (local == null) {
            synchronized (AppDataSource.class) {
                local = instance;
                if (local == null) {
                    local = fromClasspathProperties();
                    instance = local;
                }
            }
        }
        return local;
    }

    private static AppDataSource fromClasspathProperties() {
        Properties props = new Properties();
        try (InputStream is = AppDataSource.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }
        return fromProperties(props);
    }

    private static AppDataSource fromProperties(Properties props) {
        String driverClassName = getEnvOrProperty("DB_DRIVER", props.getProperty("db.driver"));
        String url = getEnvOrProperty("DB_URL", props.getProperty("db.url"));
        String username = getEnvOrProperty("DB_USERNAME", props.getProperty("db.username"));
        String password = getEnvOrProperty("DB_PASSWORD", props.getProperty("db.password"));
        Integer poolSize = parsePoolSize(getEnvOrProperty("DB_POOL_SIZE", props.getProperty("db.pool.size")));

        if (url == null || url.isBlank()) {
            // Fall back to an in-memory H2 database for developer convenience when no
            // DB configuration is provided via environment or `db.properties`.
            String fallbackUrl = "jdbc:h2:mem:campuslink;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false";
            String fallbackUser = "sa";
            String fallbackPassword = "";
            String fallbackDriver = "org.h2.Driver";
            return new AppDataSource(fallbackUrl, fallbackUser, fallbackPassword, fallbackDriver, poolSize);
        }
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Database username must be configured via DB_USERNAME or db.properties");
        }

        return new AppDataSource(
            url.trim(),
            username.trim(),
            password != null ? password : "",
            driverClassName != null ? driverClassName.trim() : null,
            poolSize
        );
    }

    private static String getEnvOrProperty(String envName, String propValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return propValue;
    }

    private static Integer parsePoolSize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return 10;
        }
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    /**
     * Builds an {@code AppDataSource} from explicit credentials instead of {@code db.properties}.
     * Intended for tests (e.g. pointing at an in-memory or throwaway test database) — does not
     * touch or replace the process-wide singleton returned by {@link #getInstance()}.
     */
    public static AppDataSource withCredentials(String url, String username, String password, String driverClassName) {
        return new AppDataSource(url, username, password, driverClassName);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return hikariDataSource.getConnection(username, password);
    }

    // --- javax.sql.DataSource plumbing this app doesn't use ---

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        // not used
    }

    @Override
    public void setLoginTimeout(int seconds) {
        // not used
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("java.util.logging not used by AppDataSource");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("Cannot unwrap to " + iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }
}
