package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection (Singleton)
 * Quản lý kết nối JDBC duy nhất đến cơ sở dữ liệu.
 */
public class DatabaseConnection {

  private Connection connection;

  private static volatile DatabaseConnection instance;

  private DatabaseConnection() {
  }

  public static DatabaseConnection getInstance() {
    if (instance == null) {
      synchronized (DatabaseConnection.class) {
        if (instance == null) {
          instance = new DatabaseConnection();
        }
      }
    }
    return instance;
  }

  public Connection getConnection() {
    try {
      if (connection == null || connection.isClosed()) {
        // Thông tin kết nối có thể đọc từ file cấu hình hoặc biến môi trường
        String url = System.getProperty("db.url", "jdbc:sqlite:auction.db");
        String username = System.getProperty("db.username", "");
        String password = System.getProperty("db.password", "");
        connection = DriverManager.getConnection(url, username, password);
      }
    } catch (SQLException e) {
      throw new RuntimeException("Không thể kết nối database: " + e.getMessage(), e);
    }
    return connection;
  }

  public void closeConnection() {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          connection.close();
        }
      } catch (SQLException e) {
        throw new RuntimeException("Không thể đóng kết nối database: " + e.getMessage(), e);
      } finally {
        connection = null;
      }
    }
  }
}