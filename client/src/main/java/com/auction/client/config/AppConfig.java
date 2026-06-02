package com.auction.client.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Client connection settings.
 * Precedence: JVM -Dserver.host / -Dserver.port &gt; env SERVER_HOST / SERVER_PORT &gt; client.properties &gt; defaults.
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());

    private static final Properties FILE_PROPS = loadClientProperties();

    private static boolean useMock = Boolean.parseBoolean(
            firstNonBlank(
                    systemProperty("app.useMock"),
                    System.getenv("APP_USE_MOCK"),
                    FILE_PROPS.getProperty("app.useMock"),
                    "false"));

    private static boolean autoFallback = Boolean.parseBoolean(
            firstNonBlank(
                    systemProperty("app.autoFallback"),
                    System.getenv("APP_AUTO_FALLBACK"),
                    FILE_PROPS.getProperty("app.autoFallback"),
                    "false"));

    private static final String serverHost = resolveHost();
    private static final int serverPort = resolvePort();

    static {
        LOGGER.info(() -> "[AppConfig] LIVE mode target: " + serverHost + ":" + serverPort
                + " (mock=" + useMock + ")");
        if (!useMock && isLocalHost(serverHost)) {
            LOGGER.warning("[AppConfig] server.host is localhost — LAN clients will NOT see the central server. "
                    + "Set server.host in client.properties or SERVER_HOST / -Dserver.host.");
        }
    }

    private AppConfig() {}

    public static boolean isUseMock() {
        return useMock;
    }

    public static void setUseMock(boolean value) {
        useMock = value;
    }

    public static boolean isAutoFallback() {
        return autoFallback;
    }

    public static String getServerHost() {
        return serverHost;
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static String getEndpointLabel() {
        return serverHost + ":" + serverPort;
    }

    public static boolean isLocalHost(String host) {
        if (host == null) {
            return true;
        }
        String h = host.trim().toLowerCase();
        return h.isEmpty() || "localhost".equals(h) || "127.0.0.1".equals(h) || "::1".equals(h);
    }

    private static Properties loadClientProperties() {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("client.properties")) {
            if (in != null) {
                props.load(in);
                LOGGER.info("[AppConfig] Loaded client.properties from classpath");
            }
        } catch (Exception e) {
            LOGGER.warning("[AppConfig] Could not load classpath client.properties: " + e.getMessage());
        }

        // Override without rebuild: place client.properties in the project folder you run mvn from
        Path external = Path.of(System.getProperty("user.dir", "."), "client.properties");
        if (Files.isRegularFile(external)) {
            try (InputStream in = Files.newInputStream(external)) {
                props.load(in);
                LOGGER.info("[AppConfig] Loaded client.properties override: " + external.toAbsolutePath());
            } catch (Exception e) {
                LOGGER.warning("[AppConfig] Could not load " + external + ": " + e.getMessage());
            }
        }

        if (props.getProperty("server.host") == null) {
            LOGGER.warning("[AppConfig] server.host not configured — will default to localhost. "
                    + "Set client/src/main/resources/client.properties or SERVER_HOST env.");
        }
        return props;
    }

    private static String resolveHost() {
        String value = firstNonBlank(
                systemProperty("server.host"),
                System.getenv("SERVER_HOST"),
                FILE_PROPS.getProperty("server.host"),
                "localhost");
        return value.trim();
    }

    private static int resolvePort() {
        String value = firstNonBlank(
                systemProperty("server.port"),
                System.getenv("SERVER_PORT"),
                FILE_PROPS.getProperty("server.port"),
                "5050");
        return parsePort(value);
    }

    private static String systemProperty(String key) {
        String value = System.getProperty(key);
        return (value == null || value.isBlank()) ? null : value;
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return "";
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value.trim());
            return (port > 0 && port <= 65535) ? port : 5050;
        } catch (Exception ignored) {
            return 5050;
        }
    }
}