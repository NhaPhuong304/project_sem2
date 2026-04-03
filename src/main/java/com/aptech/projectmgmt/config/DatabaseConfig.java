package com.aptech.projectmgmt.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConfig {

    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream in = DatabaseConfig.class
                    .getClassLoader()
                    .getResourceAsStream("database.properties");
            if (in == null) {
                throw new IOException("database.properties not found in classpath");
            }
            props.load(in);
            in.close();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.poolSize", "10")));
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setConnectionTestQuery("SELECT 1");

            dataSource = new HikariDataSource(config);

            // Test connection on startup
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    System.out.println("Connected to ProjectManagementDB successfully");
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to load database.properties: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized. Check database.properties and SQL Server connection.");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Database connection pool closed.");
        }
    }
}
