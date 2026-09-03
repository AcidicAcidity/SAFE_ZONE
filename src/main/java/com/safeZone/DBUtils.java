package com.safeZone;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.*;

public class DBUtils {
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);


    static {
        JsonReader reader = new JsonReader();
        try{
        JsonData data = reader.readDataFromJson("config.json");
        URL = "jdbc:postgresql://" + data.getHost() + ":" + data.getPort() + "/safe_zone";
        USER = data.getUser();
        PASSWORD = data.getPassword();
        } catch (IOException e) {
            log.error("ERROR READ CONFIG: " + e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() { return URL; }
}
