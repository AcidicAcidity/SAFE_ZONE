package com.safeZone.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBHelper {

    private static final Logger log = LoggerFactory.getLogger(DBHelper.class);

    private final DBUtils dbUtils;

    public DBHelper(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    public boolean checkDBConnection() {
        try (Connection conn = dbUtils.getConnection()) {
            return true;
        } catch (SQLException e) {
            log.error("Ошибка подключения к БД: {}", e.getMessage(), e);
            return false;
        }
    }

    public List<Map<String, Object>> getDataFromDB(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public int executeUpdateData(String sql, Object... params) throws SQLException {
        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            return ps.executeUpdate();
        }
    }
}
