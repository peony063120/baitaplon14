package com.auction.server.dao;

import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserDAO (Singleton)
 * Manages user data storage and retrieval.
 * Uses in-memory map AND persists to H2 database.
 */
public class UserDAO {

  private static volatile UserDAO instance;
  private final Map<String, User> store;

  private UserDAO() {
    this.store = new ConcurrentHashMap<>();
  }

  public static UserDAO getInstance() {
    if (instance == null) {
      synchronized (UserDAO.class) {
        if (instance == null) {
          instance = new UserDAO();
        }
      }
    }
    return instance;
  }

  public void saveUser(User user) {
    if (user == null || user.getUsername() == null) {
      throw new IllegalArgumentException("User or username must not be null");
    }
    // Save to in-memory map
    store.put(user.getUsername(), user);
    
    // Persist to database
    persistUserToDatabase(user);
  }

  public User findUserByUsername(String username) {
    if (username == null) return null;
    return store.get(username);
  }

  public java.util.List<User> getAllUsers() {
    return new java.util.ArrayList<>(store.values());
  }

  public User findUserById(String id) {
    if (id == null) return null;
    return store.values().stream()
        .filter(user -> id.equals(user.getId()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Persist user to H2 database.
   * Handles both insert (new user) and update (existing user).
   */
  private void persistUserToDatabase(User user) {
    String sql;
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
      conn = DatabaseConnection.getInstance().getConnection();
      
      // Check if user already exists in database
      boolean exists = userExistsInDatabase(conn, user.getId());
      
      if (exists) {
        // UPDATE existing user
        String role = user.getRole();
        if (user instanceof Bidder) {
          Bidder bidder = (Bidder) user;
          sql = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, " +
                "role = ?, balance = ?, active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, role);
            stmt.setDouble(6, bidder.getBalance());
            stmt.setBoolean(7, user.isActive());
            stmt.setString(8, user.getId());
            stmt.executeUpdate();
          }
        } else {
          // Seller or Admin (no balance field)
          sql = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, " +
                "role = ?, active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, role);
            stmt.setBoolean(6, user.isActive());
            stmt.setString(7, user.getId());
            stmt.executeUpdate();
          }
        }
      } else {
        // INSERT new user
        String role = user.getRole();
        if (user instanceof Bidder) {
          Bidder bidder = (Bidder) user;
          sql = "INSERT INTO users (id, username, password, email, full_name, role, balance, active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getFullName());
            stmt.setString(6, role);
            stmt.setDouble(7, bidder.getBalance());
            stmt.setBoolean(8, user.isActive());
            stmt.executeUpdate();
          }
        } else {
          // Seller or Admin (no balance field, default to 0)
          sql = "INSERT INTO users (id, username, password, email, full_name, role, balance, active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0.00, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getFullName());
            stmt.setString(6, role);
            stmt.setBoolean(7, user.isActive());
            stmt.executeUpdate();
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO] Failed to persist user to database: " + e.getMessage());
      // Don't fail - user is still saved in memory
    }
  }

  /**
   * Check if a user with the given ID exists in the database.
   */
  private boolean userExistsInDatabase(Connection conn, String userId) {
    if (userId == null || conn == null) return false;
    
    try (PreparedStatement stmt = conn.prepareStatement(
            "SELECT 1 FROM users WHERE id = ?")) {
      stmt.setString(1, userId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      System.err.println("[UserDAO] Failed to check user existence: " + e.getMessage());
      return false;
    }
  }
}