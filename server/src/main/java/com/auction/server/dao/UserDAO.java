package com.auction.server.dao;

import com.auction.common.entity.User;
import java.util.Map;

public class UserDAO {
  private static volatile UserDAO instance;

  private UserDAO() {}

  public static UserDAO getInstance() {
    if (instance == null) {
      synchronized (UserDAO.class) {
        if (instance == null) instance = new UserDAO();
      }
    }
    return instance;
  }

  public void saveUser(User user) {
    if (user == null || user.getUsername() == null) {
      throw new IllegalArgumentException("Invalid user data.");
    }
    Map<String, User> table = DatabaseConnection.getInstance().getTable("USERS");
    if (table.putIfAbsent(user.getUsername(), user) != null) {
      throw new IllegalArgumentException("Username already exists.");
    }
  }

  public User findUserByUsername(String username) {
    if (username == null) return null;
    Map<String, User> table = DatabaseConnection.getInstance().getTable("USERS");
    return table.get(username);
  }

  public User findUserById(String userId) {
    // Nếu ID là username, có thể dùng findUserByUsername
    // Hoặc nếu bạn lưu theo ID riêng, cần tạo map riêng
    // Ở đây giả sử userId chính là username
    return findUserByUsername(userId);
  }
}