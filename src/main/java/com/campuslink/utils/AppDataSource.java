package com.campuslink.utils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

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

    private final String url;
    private final String username;
    private final String password;

    private AppDataSource(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        if (driverClassName != null && !driverClassName.isBlank()) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("JDBC driver not found on classpath: " + driverClassName, e);
            }
        }
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

        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Database URL must be configured via DB_URL or db.properties");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Database username must be configured via DB_USERNAME or db.properties");
        }

        return new AppDataSource(url.trim(), username.trim(), password != null ? password : "", driverClassName);
    }

    private static String getEnvOrProperty(String envName, String propValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return propValue;
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
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
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
