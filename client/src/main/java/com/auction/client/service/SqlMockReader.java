package com.auction.client.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlMockReader {

  private static final Pattern INSERT_PATTERN = Pattern.compile(
    "INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*(.+?)(?:;|$)",
    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
  );

  private static final Pattern ROW_PATTERN = Pattern.compile(
    "\\(([^)]+)\\)\\s*,?"
  );

  public static class SqlMockDataSet {
    public final List<Map<String, String>> users = new ArrayList<>();
    public final List<Map<String, String>> items = new ArrayList<>();
    public final List<Map<String, String>> auctions = new ArrayList<>();
    public final List<Map<String, String>> bidTransactions = new ArrayList<>();
    public final List<Map<String, String>> autoBidConfigs = new ArrayList<>();
  }

  public static SqlMockDataSet load(String resourcePath) {
    SqlMockDataSet data = new SqlMockDataSet();
    String sql = readResource(resourcePath);
    if (sql == null) return data;

    Matcher matcher = INSERT_PATTERN.matcher(sql);
    while (matcher.find()) {
      String table = matcher.group(1).toLowerCase();
      String columnsStr = matcher.group(2);
      String valuesStr = matcher.group(3);

      List<String> columns = parseColumns(columnsStr);
      List<List<String>> rows = parseRows(valuesStr);

      List<Map<String, String>> target = getTargetList(data, table);
      if (target == null) continue;

      for (List<String> row : rows) {
        Map<String, String> rowMap = new LinkedHashMap<>();
        for (int i = 0; i < columns.size() && i < row.size(); i++) {
          rowMap.put(columns.get(i), row.get(i));
        }
        target.add(rowMap);
      }
    }
    return data;
  }

  public static SqlMockDataSet loadDefault() {
    return load("/mock-data.sql");
  }

  private static List<Map<String, String>> getTargetList(SqlMockDataSet data, String table) {
    switch (table) {
      case "users": return data.users;
      case "items": return data.items;
      case "auctions": return data.auctions;
      case "bid_transactions": return data.bidTransactions;
      case "auto_bid_configs": return data.autoBidConfigs;
      default: return null;
    }
  }

  private static String readResource(String path) {
    StringBuilder sb = new StringBuilder();
    try (InputStream is = SqlMockReader.class.getResourceAsStream(path)) {
      if (is == null) return null;
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.startsWith("--") || trimmed.startsWith("//")) continue;
        sb.append(line).append(" ");
      }
    } catch (Exception e) {
      System.err.println("[SqlMockReader] Cannot read " + path + ": " + e.getMessage());
      return null;
    }
    return sb.toString();
  }

  private static List<String> parseColumns(String cols) {
    List<String> result = new ArrayList<>();
    for (String c : cols.split(",")) {
      result.add(c.trim().toLowerCase());
    }
    return result;
  }

  static List<List<String>> parseRows(String valuesStr) {
    List<List<String>> rows = new ArrayList<>();
    int depth = 0;
    int start = -1;
    for (int i = 0; i < valuesStr.length(); i++) {
      char c = valuesStr.charAt(i);
      if (c == '(') {
        if (depth == 0) start = i + 1;
        depth++;
      } else if (c == ')') {
        depth--;
        if (depth == 0 && start != -1) {
          String rowContent = valuesStr.substring(start, i);
          rows.add(parseValues(rowContent));
          start = -1;
        }
      }
    }
    return rows;
  }

  static List<String> parseValues(String rowContent) {
    List<String> values = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inString = false;
    boolean inSingleQuote = false;

    for (int i = 0; i < rowContent.length(); i++) {
      char c = rowContent.charAt(i);

      if (c == '\'' && (i == 0 || rowContent.charAt(i-1) != '\\')) {
        inSingleQuote = !inSingleQuote;
        current.append(c);
        continue;
      }

      if (inSingleQuote) {
        current.append(c);
        continue;
      }

      if (c == ',' && !inString) {
        values.add(current.toString().trim());
        current = new StringBuilder();
      } else {
        current.append(c);
      }
    }
    String last = current.toString().trim();
    if (!last.isEmpty()) values.add(last);

    return values;
  }

  // ====================== VALUE PARSERS ======================

  public static String getString(Map<String, String> row, String col) {
    String val = row.get(col);
    if (val == null || val.equalsIgnoreCase("NULL")) return null;
    return val.replaceAll("^'|'$", "").replace("\\'", "'");
  }

  public static double getDouble(Map<String, String> row, String col) {
    String val = row.get(col);
    if (val == null || val.equalsIgnoreCase("NULL")) return 0.0;
    return Double.parseDouble(val.replaceAll("'", ""));
  }

  public static int getInt(Map<String, String> row, String col) {
    return (int) getDouble(row, col);
  }

  public static boolean getBoolean(Map<String, String> row, String col) {
    String val = row.get(col);
    if (val == null) return false;
    return val.equalsIgnoreCase("TRUE") || val.equals("1");
  }

  public static LocalDateTime getDateTime(Map<String, String> row, String col) {
    String val = row.get(col);
    if (val == null || val.equalsIgnoreCase("NULL")) return null;

    switch (val.toUpperCase()) {
      case "NOW": return LocalDateTime.now();
      case "PLUS1DAY": return LocalDateTime.now().plusDays(1);
      case "PLUS2DAY": return LocalDateTime.now().plusDays(2);
      case "PLUS3DAY": return LocalDateTime.now().plusDays(3);
      case "PLUS5DAY": return LocalDateTime.now().plusDays(5);
      case "PLUS7DAY": return LocalDateTime.now().plusDays(7);
      case "PLUS8DAY": return LocalDateTime.now().plusDays(8);
      case "PLUS9DAY": return LocalDateTime.now().plusDays(9);
      case "PLUS10DAY": return LocalDateTime.now().plusDays(10);
      case "PLUS1HOUR": return LocalDateTime.now().plusHours(1);
      case "PLUS2HOUR": return LocalDateTime.now().plusHours(2);
      case "PLUS3HOUR": return LocalDateTime.now().plusHours(3);
      case "PLUS4HOUR": return LocalDateTime.now().plusHours(4);
      case "PLUS5HOUR": return LocalDateTime.now().plusHours(5);
      case "PLUS6HOUR": return LocalDateTime.now().plusHours(6);
      case "MINUS1HOUR": return LocalDateTime.now().minusHours(1);
      case "MINUS2HOUR": return LocalDateTime.now().minusHours(2);
      case "MINUS3HOUR": return LocalDateTime.now().minusHours(3);
      case "MINUS4HOUR": return LocalDateTime.now().minusHours(4);
      case "MINUS12HOUR": return LocalDateTime.now().minusHours(12);
      case "MINUS30MIN": return LocalDateTime.now().minusMinutes(30);
      case "MINUS25MIN": return LocalDateTime.now().minusMinutes(25);
      case "MINUS15MIN": return LocalDateTime.now().minusMinutes(15);
      case "MINUS20MIN": return LocalDateTime.now().minusMinutes(20);
      case "MINUS40MIN": return LocalDateTime.now().minusMinutes(40);
      case "MINUS45MIN": return LocalDateTime.now().minusMinutes(45);
      case "MINUS50MIN": return LocalDateTime.now().minusMinutes(50);
      case "MINUS55MIN": return LocalDateTime.now().minusMinutes(55);
      case "MINUS60MIN": return LocalDateTime.now().minusMinutes(60);
      case "MINUS80MIN": return LocalDateTime.now().minusMinutes(80);
      case "MINUS100MIN": return LocalDateTime.now().minusMinutes(100);
      case "MINUS120MIN": return LocalDateTime.now().minusMinutes(120);
      case "MINUS150MIN": return LocalDateTime.now().minusMinutes(150);
      case "MINUS200MIN": return LocalDateTime.now().minusMinutes(200);
      case "MINUS1DAY": return LocalDateTime.now().minusDays(1);
      case "MINUS2DAY": return LocalDateTime.now().minusDays(2);
      case "MINUS3DAY": return LocalDateTime.now().minusDays(3);
    }
    return LocalDateTime.now();
  }
}
