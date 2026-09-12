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
import java.time.LocalDateTime;


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

    // ПРОВЕРКА НА АВТОРИЗАЦИЮ
    public boolean userAuth(String login, String password) throws SQLException {
        if (login == null || login.isBlank() ||
            password == null || password.isBlank()) {
            log.error("Неверный логин или пароль");
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE login = ? AND password =? LIMIT 1";
        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1,login);
            ps.setString(2,password);
            try (ResultSet rs = ps.executeQuery()){
                return rs.next();
            }

        } catch (SQLException e) {
            log.error("Ошибка подключения к БД: {}", e.getMessage(), e);
            return false;
        }
    }
// Добавление пользователя в базу данных
    public void addUser(String login, String password) throws SQLException  {
        if (login == null || login.isBlank()||
            password == null || password.isBlank()){
            log.error("Пустой логин или пароль — пользователь не добавлен");
            return;
            }
        String sql = "INSERT INTO users (login, password, status, role) VALUES (?,?,active,client)";
        try (Connection conn = dbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1,login);
            ps.setString(2,password);

            ps.executeUpdate();
            log.info("Вы успешно зарегестрированны!");

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())){
                log.error("Такой логин '{}' уже занят");
            }else{
                log.error("Ошибка добавления пользователя: {}", e.getMessage(), e);
            }
        }
    }

    public int createPayment(LocalDateTime rentTime, String size, int binId, String token) {
        // Создание платежа в базе данных
        // Возвращает ID созданного платежа
        return 0;
    }

    public String getPaymentStatus(int paymentId) {
        // Получение статуса платежа
        return null;
    }

    public List<Map<String, Object>> getPayments(LocalDateTime rentTime, LocalDateTime createdAt, LocalDateTime endRentDate, int binId, int userId) {
        return null; // Поиск платежей с фильтрацией или все что есть
    }

    public int findFreeBin(String size, LocalDateTime rentTime) {
        // Поиск свободного бинна для размера и времени аренды
        // Возвращает ID найденной ячейки = Int(pos_x + pos_y)
        return 0;
    }
}
