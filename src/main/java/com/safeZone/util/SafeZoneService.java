package com.safeZone.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.safeZone.model.Bin;
import com.safeZone.model.Payment;
import com.safeZone.model.User;

public class SafeZoneService {

    private static final Logger log = LoggerFactory.getLogger(SafeZoneService.class);

    private final DBHelper db;

    public SafeZoneService(DBHelper db) {
        this.db = db;
    }

    // createEntity нужен для создания сущностей, это значит с помощью его мы делаем в таблице запросы на добовление юзеров, платежей и тд
    // Возвращает true, если запись создана
    public boolean createEntity(EntityType type, Map<String, Object> p) throws SQLException {
        if (p == null) {
            log.warn("createEntity: params == null, type = {}", type);
            return false;
        }
        String sql;
        List<Object> args = new ArrayList<>();
        switch (type) {
            case USER -> {
                if (isBlank(p.get("login")) || isBlank(p.get("password"))) {
                    log.warn("createEntity USER: пустой login или password");
                    return false;
                }
                sql = "INSERT INTO users (login, password, status, role) " +
                      "VALUES (?, ?, 'active', 'client') RETURNING id";
                args.add(p.get("login"));
                args.add(PasswordUtil.hash(p.get("password").toString()));
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
            int id = db.executeInsertReturning(sql, args.toArray());
            return id > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                log.warn("createEntity {}: дубликат (unique violation)", type);
            } else {
                log.error("createEntity {}: ошибка {}", type, e.getMessage(), e);
            }
            throw e;
        }
    }

    // А этот метод нужен для проверки всех этих сущностей
    // Возвращает объект (User/Bin/Payment) или null, если ничего не нашлось
    @SuppressWarnings("unchecked")
    public <T> T checkEntity(EntityType type, Map<String, Object> p) throws SQLException {
        if (p == null) {
            log.warn("checkEntity: params == null, type = {}", type);
            return null;
        }
        switch (type) {
            case USER -> {
                if (isBlank(p.get("login")) || isBlank(p.get("password"))) return null;
                String sql = "SELECT * FROM users " +
                             "WHERE login = ? AND status = 'active' LIMIT 1";
                Map<String, Object> row = db.getSingleRow(sql, p.get("login"));
                if (row == null) return null;
                Object hash = row.get("password");
                if (hash == null || !PasswordUtil.verify(
                        p.get("password").toString(), hash.toString())) return null;
                return (T) mapUserFromRow(row);
            }
            case PAYMENT -> {
                String sql = "SELECT * FROM payments " +
                             "WHERE order_id = ? AND UPPER(status) = 'PAID' LIMIT 1";
                Map<String, Object> row = db.getSingleRow(sql, p.get("Order_ID"));
                return row == null ? null : (T) mapPaymentFromRow(row);
            }
            case BIN -> {
                String sql = "SELECT * FROM bins " +
                             "WHERE size = ? AND status = 'true' LIMIT 1";
                Map<String, Object> row = db.getSingleRow(sql, p.get("size"));
                return row == null ? null : (T) mapBinFromRow(row);
            }
            default -> throw new IllegalArgumentException("Неизвестный тип: " + type);
        }
    }

    // АРТЕМ ВОТ ЧТО ТЫ ХОТЕЛ, МЕТОД ДЛЯ ИЗМЕНЕНИЯ СУЩНОСТИ
    // Возвращает true, если что-то обновилось
    public boolean ChangeEntity(EntityType type, int id, Map<String, Object> p) throws SQLException {
        if (p == null || p.isEmpty()) {
            log.error("ChangeEntity: пустые params, type = {}, id = {}", type, id);
            return false;
        }
        String table;
        String idColumn;
        Map<String, String> allowedFields = new LinkedHashMap<>();

        switch (type) {
            case USER -> {
                table = "users";
                idColumn = "id";
                allowedFields.put("login", "login");
                allowedFields.put("password", "password");
                allowedFields.put("status", "status");
                allowedFields.put("role", "role");
            }
            case BIN -> {
                table = "bins";
                idColumn = "bin_id";
                allowedFields.put("PriceAtHour", "priceathour");
                allowedFields.put("pos_x", "pos_x");
                allowedFields.put("pos_y", "pos_y");
                allowedFields.put("size", "size");
                allowedFields.put("status", "status");
            }
            case PAYMENT -> {
                table = "payments";
                idColumn = "order_id";
                allowedFields.put("Bin_ID", "bin_id");
                allowedFields.put("User_ID", "user_id");
                allowedFields.put("PriceAtHour", "priceathour");
                allowedFields.put("status", "status");
                allowedFields.put("end_rent_date", "end_rent_date");
                allowedFields.put("rentTime", "\"rentTime\"");
            }
            default -> throw new IllegalArgumentException("Неизвестный тип: " + type);
        }

        StringBuilder sql = new StringBuilder("UPDATE " + table + " SET ");
        List<Object> args = new ArrayList<>();
        boolean first = true;

        for (Map.Entry<String, String> e : allowedFields.entrySet()) {
            if (p.containsKey(e.getKey())) {
                if (!first) sql.append(", ");
                sql.append(e.getValue()).append(" = ?");
                Object val = p.get(e.getKey());
                if ("password".equals(e.getKey()) && val != null) {
                    val = PasswordUtil.hash(val.toString());
                }
                args.add(val);
                first = false;
            }
        }

        if (first) {
            log.warn("ChangeEntity {}: ни одно поле не совпало с допустимыми", type);
            return false;
        }

        // у платежа update_at всегда проставляем NOW()
        if (type == EntityType.PAYMENT) {
            sql.append(", update_at = NOW()");
        }

        sql.append(" WHERE ").append(idColumn).append(" = ?");
        args.add(id);

        try {
            return db.executeUpdateData(sql.toString(), args.toArray()) > 0;
        } catch (SQLException e) {
            log.error("ChangeEntity {} id={}: {}", type, id, e.getMessage(), e);
            throw e;
        }
    }

    // Этот метод нужен для поиска Айди ячеек по x и y
    // Возвращает Bin или Payment (или null)
    @SuppressWarnings("unchecked")
    public <T> T findIdByPosition(EntityType type, int posX, int posY) throws SQLException {
        String sql = switch (type) {
            case BIN -> "SELECT * FROM bins WHERE pos_x = ? AND pos_y = ? LIMIT 1";
            case PAYMENT -> "SELECT p.* FROM payments p " +
                            "JOIN bins b ON p.bin_id = b.bin_id " +
                            "WHERE b.pos_x = ? AND b.pos_y = ? LIMIT 1";
            default -> throw new IllegalArgumentException(
                    "findIdByPosition только для BIN или PAYMENT, дано: " + type);
        };
        Map<String, Object> row = db.getSingleRow(sql, posX, posY);
        if (row == null) return null;
        return type == EntityType.BIN
                ? (T) mapBinFromRow(row)
                : (T) mapPaymentFromRow(row);
    }

    // Так это уже будет поиск свободных ячеек под размер и время
    // Возвращает Bin или null
    public Bin findFreeBin(String size, LocalDateTime endRentDate) throws SQLException {
        String sql = "SELECT b.* FROM bins b " +
                     "WHERE b.size = ? AND b.status = 'true' " +
                     "AND NOT EXISTS ( " +
                     "  SELECT 1 FROM payments p " +
                     "  WHERE p.bin_id = b.bin_id " +
                     "  AND UPPER(p.status) IN ('PENDING', 'PAID') " +
                     "  AND p.end_rent_date > ? " +
                     ") LIMIT 1";
        Map<String, Object> row = db.getSingleRow(sql, size, Timestamp.valueOf(endRentDate));
        return row == null ? null : mapBinFromRow(row);
    }

    // Мапперы из Map (для методов выше) 
    public User mapUserFromRow(Map<String, Object> r) {
        return new User(
                ((Number) r.get("id")).intValue(),
                (String) r.get("login"),
                (String) r.get("password"),
                User.Status.getStatus(statusToInt((String) r.get("status"))),
                User.Role.getRole(roleToInt((String) r.get("role")))
        );
    }

    public Bin mapBinFromRow(Map<String, Object> r) {
        return new Bin(
                ((Number) r.get("bin_id")).intValue(),
                ((Number) r.get("priceathour")).intValue(),
                ((Number) r.get("pos_x")).intValue(),
                ((Number) r.get("pos_y")).intValue(),
                (String) r.get("size"),
                "true".equalsIgnoreCase((String) r.get("status"))
        );
    }

    public Payment mapPaymentFromRow(Map<String, Object> r) {
        Bin stubBin = new Bin(
                ((Number) r.get("bin_id")).intValue(),
                ((Number) r.get("priceathour")).intValue(),
                0, 0, "unknown", false);
        User stubUser = new User(
                ((Number) r.get("user_id")).intValue(),
                null, null, User.Status.ACTIVE, User.Role.CLIENT);
        return new Payment(
                ((Number) r.get("order_id")).intValue(),
                stubBin,
                stubBin,
                Payment.StatusOrder.getStatusOrder(
                        statusOrderToInt((String) r.get("status"))),
                null,
                toLdt(r.get("created_at")),
                toLdt(r.get("update_at")),
                toLdt(r.get("end_rent_date")),
                stubUser
        );
    }

    // А тут уже наши любимые мапперы из ResultSet
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

    // Обёртки для Егора
    public boolean createUser(String login, String password) throws SQLException {
        return createEntity(EntityType.USER, Map.of(
                "login", login, "password", password));
    }

    public User authUser(String login, String password) throws SQLException {
        return checkEntity(EntityType.USER, Map.of(
                "login", login, "password", password));
    }

    public boolean createPayment(int binId, int userId, int priceAtHour,
                                 LocalDateTime endRentDate, int rentTime) throws SQLException {
        return createEntity(EntityType.PAYMENT, Map.of(
                "Bin_ID", binId,
                "User_ID", userId,
                "PriceAtHour", priceAtHour,
                "end_rent_date", Timestamp.valueOf(endRentDate),
                "rentTime", rentTime));
    }

    public Payment getPaidPayment(int orderId) throws SQLException {
        return checkEntity(EntityType.PAYMENT, Map.of("Order_ID", orderId));
    }

    public boolean ChangeUserStatus(int userId, String status) throws SQLException {
        return ChangeEntity(EntityType.USER, userId, Map.of("status", status));
    }

    public boolean ChangeBinStatus(int binId, boolean free) throws SQLException {
        return ChangeEntity(EntityType.BIN, binId,
                Map.of("status", free ? "true" : "false"));
    }

    public boolean ChangePaymentStatus(int orderId, String status) throws SQLException {
        return ChangeEntity(EntityType.PAYMENT, orderId, Map.of("status", status));
    }

    // Приватные хелперы 
    private static boolean isBlank(Object o) {
        return o == null || o.toString().isBlank();
    }

    private static LocalDateTime toLdt(Object o) {
        if (o == null) return null;
        if (o instanceof Timestamp t) return t.toLocalDateTime();
        if (o instanceof LocalDateTime l) return l;
        return null;
    }

    private static int statusToInt(String s) {
        if (s == null) return 0;
        return switch (s.toLowerCase()) {
            case "active"  -> 1;
            case "blocked" -> 2;
            case "deleted" -> 3;
            default -> throw new IllegalArgumentException("Неизвестный статус юзера: " + s);
        };
    }

    private static int roleToInt(String s) {
        if (s == null) return 0;
        return switch (s.toLowerCase()) {
            case "client" -> 1;
            case "admin"  -> 2;
            default -> throw new IllegalArgumentException("Неизвестная роль: " + s);
        };
    }

    private static int statusOrderToInt(String s) {
        if (s == null) return 0;
        return switch (s.toLowerCase()) {
            case "pending"   -> 1;
            case "paid"      -> 2;
            case "cancelled" -> 3;
            case "refunded"  -> 4;
            default -> throw new IllegalArgumentException("Неизвестный статус платежа: " + s);
        };
    }
}