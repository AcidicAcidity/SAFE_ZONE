package com.safeZone;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.slf4j.*;

public class DBHelper {
    private static final Logger log = LoggerFactory.getLogger(DBHelper.class);
    public static void SqlExecRequest(String jDBUrl, String user, String password, String sqlRequest) throws Exception {
        try (Connection conn = DriverManager.getConnection(jDBUrl, user, password);
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlRequest);
            System.out.println("SUCCESS EXECUTE SQL REQUEST: " + sqlRequest);
            log.info("SUCCESS EXECUTE SQL REQUEST: " + sqlRequest);
        } catch (Exception e) {
            log.info("ERROR EXECUTING SQL REQUEST" + e);
        }
    }

}
