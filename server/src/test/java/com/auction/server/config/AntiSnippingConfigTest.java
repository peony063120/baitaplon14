package com.auction.server.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AntiSnipingConfigTest {

    @Test
    void testGetInstance_ShouldReturnSameInstance() {
        AntiSnipingConfig config1 = AntiSnipingConfig.getInstance();
        AntiSnipingConfig config2 = AntiSnipingConfig.getInstance();
        assertSame(config1, config2, "Singleton should return same instance");
    }

    @Test
    void testGetThresholdSeconds_ShouldReturnDefaultValue() {
        AntiSnipingConfig config = AntiSnipingConfig.getInstance();
        int threshold = config.getThresholdSeconds();
        assertTrue(threshold > 0, "Threshold should be positive");
    }

    @Test
    void testGetExtensionSeconds_ShouldReturnDefaultValue() {
        AntiSnipingConfig config = AntiSnipingConfig.getInstance();
        int extension = config.getExtensionSeconds();
        assertTrue(extension > 0, "Extension should be positive");
    }
}