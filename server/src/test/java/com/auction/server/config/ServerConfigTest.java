package com.auction.server.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @Test
    void testGetInstance_ShouldReturnSameInstance() {
        ServerConfig config1 = ServerConfig.getInstance();
        ServerConfig config2 = ServerConfig.getInstance();
        assertSame(config1, config2);
    }

    @Test
    void testGetPort_ShouldReturnDefaultPort() {
        ServerConfig config = ServerConfig.getInstance();
        int port = config.getPort();
        assertTrue(port > 0 && port < 65536, "Port should be between 1 and 65535");
    }
}