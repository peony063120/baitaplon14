package com.auction.server.dao;

import com.auction.server.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection (Singleton)
 * Quan ly ket noi JDBC duy nhat den co so du lieu.
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
        DatabaseConfig config = DatabaseConfig.getInstance();
        String driver = System.getProperty("db.driver", config.getDriver());
        if (driver != null && !driver.isBlank()) {
          Class.forName(driver);
        }

        String url = System.getProperty("db.url", config.getUrl());
        String username = System.getProperty("db.username", config.getUsername());
        String password = System.getProperty("db.password", config.getPassword());
        connection = DriverManager.getConnection(url, username, password);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Khong tim thay JDBC driver: " + e.getMessage(), e);
    } catch (SQLException e) {
      throw new RuntimeException("Khong the ket noi database: " + e.getMessage(), e);
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
        throw new RuntimeException("Khong the dong ket noi database: " + e.getMessage(), e);
      } finally {
        connection = null;
      }
    }
  }
}
