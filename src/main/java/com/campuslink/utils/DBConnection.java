package com.campuslink.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static volatile DBConnection instance;
    private Connection connection;
    private final Properties props = new Properties();

    private DBConnection() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new RuntimeException("application.properties not found in classpath");
            }
            props.load(is);
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    public static DBConnection getInstance() {
        DBConnection local = instance;
        if (local == null) {
            synchronized (DBConnection.class) {
                local = instance;
                if (local == null) {
                    local = new DBConnection();
                    instance = local;
                }
            }
        }
        return local;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (this) {
                    if (connection == null || connection.isClosed()) {
                        connection = DriverManager.getConnection(
                            props.getProperty("db.url"),
                            props.getProperty("db.username"),
                            props.getProperty("db.password")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
        return connection;
    }

    public void closeConnection() {
        synchronized (this) {
            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                } finally {
                    connection = null;
                }
            }
        }
    }
}
