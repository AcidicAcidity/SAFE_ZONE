package com.safezone.infrastructure.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DBHelper {

    private final Properties env = envRead.readEnv();

    private final String url = env.getProperty("DB_URL");
    private final String user = env.getProperty("DB_USER");
    private final String password = env.getProperty("DB_PASSWORD");

    public List<Map<String, Object>> getDataFromDB(
            String sql,
            Object... params) throws SQLException {

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {

                    Map<String, Object> row = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        row.put(
                                meta.getColumnLabel(i),
                                rs.getObject(i)
                        );
                    }

                    rows.add(row);
                }
            }
        }

        return rows;
    }

    public Map<String, Object> getSingleRow(
            String sql,
            Object... params) throws SQLException {

        List<Map<String, Object>> rows =
                getDataFromDB(sql, params);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public int executeUpdateData(
            String sql,
            Object... params) throws SQLException {

        try (Connection conn =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            return ps.executeUpdate();
        }
    }

    public int executeInsertReturning(
            String sql,
            Object... params) throws SQLException {

        try (Connection conn =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }
}