package com.auction.server.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * DatabaseConfig
 * Cấu hình kết nối database.
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
                // Giá trị mặc định (ví dụ H2 in-memory)
                props.setProperty("db.url", "jdbc:h2:./data/auctiondb;INIT=RUNSCRIPT FROM 'classpath:schema.sql'");
                props.setProperty("db.user", "sa");
                props.setProperty("db.password", "");
                props.setProperty("db.driver", "org.h2.Driver");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public String getUser() {
        return props.getProperty("db.user");
    }

    public String getPassword() {
        return props.getProperty("db.password");
    }

    public String getDriver() {
        return props.getProperty("db.driver");
    }
}