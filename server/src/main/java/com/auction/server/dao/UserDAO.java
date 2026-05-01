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
      throw new IllegalArgumentException("Dữ liệu người dùng không hợp lệ."); // Xử lý lỗi đầu vào
    }

    Map<String, User> table = DatabaseConnection.getInstance().getConnection().getTable("USERS");

    // Đảm bảo không ghi đè người dùng đã tồn tại (Atomicity)
    if (table.putIfAbsent(user.getUsername(), user) != null) {
      throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
    }
  }

  public User findUserByUsername(String username) {
    if (username == null) return null;
    Map<String, User> table = DatabaseConnection.getInstance().getConnection().getTable("USERS");
    return table.get(username);
  }
}