package com.safeZone.util;

import com.safeZone.model.Bin;
import com.safeZone.model.Payment;
import com.safeZone.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SafeZoneService {

    private static final Logger log = LoggerFactory.getLogger(SafeZoneService.class);

    private final DBHelper db;

    public SafeZoneService(DBHelper db) {
        this.db = db;
    }
        // createEntity нужен для осдание сущностей, это значит с помощью его мы делаем в таблице запросы на добовление юзеров, платежей и тд
        public int createEntity(EntityType type, Map<String, Object> p) throws SQLException {
        if (p == null) {
            log.warn("createEntity: params == null, type = {}", type);
            return -1;
        }

        String sql;
        List<Object> args = new ArrayList<>();

        switch (type) {
            case USER -> {
                if (isBlank(p.get("login")) || isBlank(p.get("password"))) {
                    log.warn("createEntity USER: пустой login или password");
                    return -1;
                }
                sql = "INSERT INTO users (login, password, status, role) " +
                      "VALUES (?, ?, 'active', 'client') RETURNING id";
                args.add(p.get("login"));
                args.add(p.get("password"));
            }
            case PAYMENT -> {
                sql = "INSERT INTO payments (bin_id, user_id, priceathour, status, " +
                      "created_at, end_rent_date, \"rentTime\") " +
                      "VALUES (?, ?, ?, 'pending', NOW(), ?, ?) RETURNING order_id";
                args.add(p.get("Bin_ID"));
                args.add(p.get("User_ID"));
                args.add(p.get("PriceAtHour"));
                args.add(p.get("end_rent_date"));
                args.add(p.get("rentTime"));
            }
            case BIN -> {
                sql = "INSERT INTO bins (priceathour, pos_x, pos_y, size, status) " +
                      "VALUES (?, ?, ?, ?, 'true') RETURNING bin_id";
                args.add(p.get("PriceAtHour"));
                args.add(p.get("pos_x"));
                args.add(p.get("pos_y"));
                args.add(p.get("size"));
            }
            default -> throw new IllegalArgumentException("Неизвестный тип: " + type);
        }

        try {
            return db.executeInsertReturning(sql, args.toArray());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                log.warn("createEntity {}: дубликат (unique violation)", type);
            } else {
                log.error("createEntity {}: ошибка {}", type, e.getMessage(), e);
            }
            throw e;
        }
    }
    //А этот метод нужен для проверки всех этих сущностей 
        public boolean checkEntity(EntityType type, Map<String, Object> p) throws SQLException {
        if (p == null) {
            log.warn("checkEntity: params == null, type = {}", type);
            return false;
        }

        String sql;
        List<Object> args = new ArrayList<>();

        switch (type) {
            case USER -> {
                if (isBlank(p.get("login")) || isBlank(p.get("password"))) {
                    return false;
                }
                sql = "SELECT 1 FROM users " +
                      "WHERE login = ? AND password = ? AND status = 'active' LIMIT 1";
                args.add(p.get("login"));
                args.add(p.get("password"));
            }
            case PAYMENT -> {
                sql = "SELECT 1 FROM payments " +
                      "WHERE order_id = ? AND UPPER(status) = 'PAID' LIMIT 1";
                args.add(p.get("Order_ID"));
            }
            case BIN -> {
                sql = "SELECT 1 FROM bins " +
                      "WHERE size = ? AND status = 'true' LIMIT 1";
                args.add(p.get("size"));
            }
            default -> throw new IllegalArgumentException("Неизвестный тип: " + type);
        }

        try {
            List<Map<String, Object>> rows = db.getDataFromDB(sql, args.toArray());
            return !rows.isEmpty();
        } catch (SQLException e) {
            log.error("checkEntity {}: {}", type, e.getMessage(), e);
            return false;
        }
    }
    //Этот метод нужен для поиска Айди ячеек по x и y
        public Integer findIdByPosition(EntityType type, int posX, int posY) throws SQLException {
        String sql = switch (type) {
            case BIN -> "SELECT bin_id FROM bins " +
                        "WHERE pos_x = ? AND pos_y = ? LIMIT 1";
            case PAYMENT -> "SELECT p.order_id FROM payments p " +
                            "JOIN bins b ON p.bin_id = b.bin_id " +
                            "WHERE b.pos_x = ? AND b.pos_y = ? LIMIT 1";
            default -> throw new IllegalArgumentException(
                    "findIdByPosition работает только с BIN или PAYMENT, дано: " + type);
        };

        List<Map<String, Object>> rows = db.getDataFromDB(sql, posX, posY);
        if (rows.isEmpty()) return null;
        Object id = rows.get(0).values().iterator().next();
        return ((Number) id).intValue();
    }
    // так это уже будет поиск свободных ячеек под размер и время 
        public Integer findFreeBin(String size, LocalDateTime endRentDate) throws SQLException {
        String sql = "SELECT b.bin_id FROM bins b " +
                     "WHERE b.size = ? AND b.status = 'true' " +
                     "AND NOT EXISTS ( " +
                     "  SELECT 1 FROM payments p " +
                     "  WHERE p.bin_id = b.bin_id " +
                     "  AND UPPER(p.status) IN ('PENDING', 'PAID') " +
                     "  AND p.end_rent_date > ? " +
                     ") LIMIT 1";

        List<Map<String, Object>> rows = db.getDataFromDB(sql, size,
                Timestamp.valueOf(endRentDate));
        if (rows.isEmpty()) return null;
        Object id = rows.get(0).values().iterator().next();
        return ((Number) id).intValue();
    }
    //а тут уже наши любимые мапперы
        public User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("password"),
                User.Status.getStatus(statusToInt(rs.getString("status"))),
                User.Role.getRole(roleToInt(rs.getString("role")))
        );
    }

    public Bin mapBin(ResultSet rs) throws SQLException {
        return new Bin(
                rs.getInt("bin_id"),
                rs.getInt("priceathour"),
                rs.getInt("pos_x"),
                rs.getInt("pos_y"),
                rs.getString("size"),
                "true".equalsIgnoreCase(rs.getString("status"))
        );
    }

    public Payment mapPayment(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updateTs  = rs.getTimestamp("update_at");
        Timestamp endTs     = rs.getTimestamp("end_rent_date");

        Bin stubBin = new Bin(rs.getInt("bin_id"), rs.getInt("priceathour"),
                              0, 0, "unknown", false);
        User stubUser = new User(rs.getInt("user_id"), null, null,
                                 User.Status.ACTIVE, User.Role.CLIENT);

        return new Payment(
                rs.getInt("order_id"),
                stubBin,
                stubBin,
                Payment.StatusOrder.getStatusOrder(
                        statusOrderToInt(rs.getString("status"))),
                null,
                createdTs != null ? createdTs.toLocalDateTime() : null,
                updateTs  != null ? updateTs.toLocalDateTime()  : null,
                endTs     != null ? endTs.toLocalDateTime()     : null,
                stubUser
        );
    }

}