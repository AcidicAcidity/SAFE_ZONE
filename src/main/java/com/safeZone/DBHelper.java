package com.safeZone;

import java.sql.*;
import org.slf4j.*;
import java.util.*;

public class DBHelper {
    // Пока не используется логгер, но не удаляем, возможно нужно будет добавить логирование
    private static final Logger log = LoggerFactory.getLogger(DBHelper.class);
    // public static void SqlExecRequest(String jDBUrl, String user, String password, String sqlRequest) throws Exception {
    //     try (Connection conn = DriverManager.getConnection(jDBUrl, user, password);
    //             Statement stmt = conn.createStatement()) {
    //         stmt.executeUpdate(sqlRequest);
    //         System.out.println("SUCCESS EXECUTE SQL REQUEST: " + sqlRequest);
    //         log.info("SUCCESS EXECUTE SQL REQUEST: " + sqlRequest);
    //     } catch (Exception e) {
    //         log.info("ERROR EXECUTING SQL REQUEST" + e);
    //     }
    // }

    public static boolean CheckDBConnection() {
        try (Connection conn = DBUtils.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static List<Map<String, Object>> getDataFromDB(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = DBUtils.getConnection();
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
                        String columnName = meta.getColumnLabel(i);

                        row.put(columnName, rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

}
