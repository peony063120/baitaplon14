package com.auction.server.dao;

import java.util.Map;

interface Connection {
  <K, V> Map<K, V> getTable(String tableName);
}