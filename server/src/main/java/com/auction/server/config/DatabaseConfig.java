package com.auction.server.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * DatabaseConfig
 * Cau hinh ket noi database.
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private final Properties props;

    private DatabaseConfig() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/auction");
                props.setProperty("db.username", "root");
                props.setProperty("db.password", "");
                props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            }
        } catch (Exception e) {
            throw new RuntimeException("Khong the doc cau hinh database", e);
        }
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public String getUrl() {
        return props.getProperty("db.url");
    }

    public String getUsername() {
        return props.getProperty("db.username", props.getProperty("db.user", ""));
    }

    public String getPassword() {
        return props.getProperty("db.password", "");
    }

    public String getDriver() {
        return props.getProperty("db.driver");
    }
}
