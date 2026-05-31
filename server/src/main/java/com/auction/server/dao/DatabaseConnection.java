package com.auction.server.dao;

import com.auction.server.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
  private Connection connection;
  private static volatile DatabaseConnection instance;

  private DatabaseConnection() {}

  public static DatabaseConnection getInstance() {
    if (instance == null) {
      synchronized (DatabaseConnection.class) {
        if (instance == null) instance = new DatabaseConnection();
      }
    }
    return instance;
  }

  public Connection getConnection() {
    try {
      if (connection == null || connection.isClosed()) {
        DatabaseConfig cfg = DatabaseConfig.getInstance();
        // Cho phép override bằng -Ddb.url=... khi chạy java -jar
        String url      = System.getProperty("db.url",      cfg.getUrl());
        String user     = System.getProperty("db.username", cfg.getUser());
        String password = System.getProperty("db.password", cfg.getPassword());
        String driver   = System.getProperty("db.driver",   cfg.getDriver());

        Class.forName(driver);                       // nạp driver H2
        connection = DriverManager.getConnection(url, user, password);
        System.out.println("[DB] Connected: " + url);
      }
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException("Không thể kết nối database: " + e.getMessage(), e);
    }
    return connection;
  }

  public void closeConnection() {
    if (connection != null) {
      try { if (!connection.isClosed()) connection.close(); }
      catch (SQLException e) { throw new RuntimeException(e); }
      finally { connection = null; }
    }
  }
}
