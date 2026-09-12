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
    
}