package com.auction.server.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * ServerConfig
 * Đọc cấu hình từ file (ví dụ: server.properties).
 */
public class ServerConfig {
    private static ServerConfig instance;
    private final Properties props;

    private ServerConfig() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Giá trị mặc định
                props.setProperty("server.port", "8080");
                props.setProperty("server.host", "localhost");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }

    public int getPort() {
        return Integer.parseInt(props.getProperty("server.port", "8080"));
    }

    public String getHost() {
        return props.getProperty("server.host", "localhost");
    }
}