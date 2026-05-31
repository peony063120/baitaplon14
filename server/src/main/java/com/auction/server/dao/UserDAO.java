package com.auction.server.dao;

import com.auction.common.entity.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserDAO (Singleton)
 * Quản lý lưu trữ và truy xuất dữ liệu người dùng.
 * Sử dụng in-memory map nội bộ.
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
      throw new IllegalArgumentException("User hoặc username không được null");
    }
    store.put(user.getUsername(), user);
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
}