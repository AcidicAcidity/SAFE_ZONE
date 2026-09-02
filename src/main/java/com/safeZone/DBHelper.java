package com.safeZone;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBHelper {
    public static void initSchema(String jDBUrl, String user, String password, String sqlRequest) throws Exception {
        try (Connection conn = DriverManager.getConnection(jDBUrl, user, password);
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlRequest);
            System.out.println("БД была создана успешно");
        }
    }
}
