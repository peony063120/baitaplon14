package com.auction.server.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {

    @Test
    void testGetInstance_ShouldReturnSameInstance() {
        DatabaseConfig config1 = DatabaseConfig.getInstance();
        DatabaseConfig config2 = DatabaseConfig.getInstance();
        assertSame(config1, config2);
    }

    @Test
    void testGetUrl_ShouldReturnNonNull() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        String url = config.getUrl();
        assertNotNull(url, "Database URL should not be null");
    }
}