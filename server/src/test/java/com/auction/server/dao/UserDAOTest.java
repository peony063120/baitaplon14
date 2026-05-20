package com.auction.server.dao;

import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        // Reset singleton for testing (using reflection or recreate)
        userDAO = UserDAO.getInstance();
    }

    @Test
    void testSaveAndFindUser() {
        Bidder bidder = new Bidder("testuser", "pass123", "test@example.com", "Test User");
        bidder.addBalance(1000.0);

        userDAO.saveUser(bidder);

        User found = userDAO.findUserByUsername("testuser");
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testFindUserByUsername_NotFound() {
        User found = userDAO.findUserByUsername("nonexistent");
        assertNull(found);
    }
}