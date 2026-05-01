package com.auction.server.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseConnection {
  private static volatile DatabaseConnection instance;
  private final Connection connection;

  private DatabaseConnection() {
    final Map<String, Map<?, ?>> masterStorage = new ConcurrentHashMap<>();

    this.connection = new Connection() {
      @Override
      @SuppressWarnings("unchecked")
      public <K, V> Map<K, V> getTable(String tableName) {
        return (Map<K, V>) masterStorage.computeIfAbsent(tableName, k -> new ConcurrentHashMap<>());
      }
    };
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
    return this.connection;
  }
}