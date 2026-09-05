package com.safeZone.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {

    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    private final String url;
    private final String user;
    private final String password;

    public DBUtils() throws IOException {
        this("config.json");
    }

    // Отдельный конструктор с именем ресурса
    public DBUtils(String configResourceName) throws IOException {
        JsonReader reader = new JsonReader();
        JsonData data = reader.readDataFromJson(configResourceName);

        if (data.getHost() == null || data.getUser() == null || data.getPassword() == null) {
            throw new IOException("В конфиге отсутствуют обязательные поля (host/user/password): " + configResourceName);
        }

        this.url = "jdbc:postgresql://" + data.getHost() + ":" + data.getPort() + "/safe_zone";
        this.user = data.getUser();
        this.password = data.getPassword();

        log.info("DBUtils инициализирован для {}:{}", data.getHost(), data.getPort());
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public String getUrl() {
        return url;
    }
}
