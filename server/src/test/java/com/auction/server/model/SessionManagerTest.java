package com.auction.server.model;

import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager manager;
    private User testUser;

    @BeforeEach
    void setUp() {
        manager = SessionManager.getInstance();
        testUser = new Bidder("testuser", "pass", "test@example.com", "Test User");
    }

    @Test
    void testGetInstance_ShouldReturnSameInstance() {
        SessionManager manager2 = SessionManager.getInstance();
        assertSame(manager, manager2);
    }

    @Test
    void testCreateAndGetSession() {
        String sessionId = manager.createSession(testUser);
        assertNotNull(sessionId);

        User retrieved = manager.getUser(sessionId);
        assertNotNull(retrieved);
        assertEquals("testuser", retrieved.getUsername());
    }

    @Test
    void testInvalidateSession() {
        String sessionId = manager.createSession(testUser);
        manager.invalidateSession(sessionId);

        User retrieved = manager.getUser(sessionId);
        assertNull(retrieved);
    }
}