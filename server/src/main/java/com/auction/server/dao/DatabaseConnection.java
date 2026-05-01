package com.auction.server.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseConnection {
  private static volatile DatabaseConnection instance;
  private final Map<String, Map<String, Object>> storage;

  private DatabaseConnection() {
    storage = new ConcurrentHashMap<>();
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

  @SuppressWarnings("unchecked")
  public <T> Map<String, T> getTable(String tableName) {
    return (Map<String, T>) storage.computeIfAbsent(tableName, k -> new ConcurrentHashMap<>());
  }

  public void closeConnection() {
    storage.clear();
  }
}