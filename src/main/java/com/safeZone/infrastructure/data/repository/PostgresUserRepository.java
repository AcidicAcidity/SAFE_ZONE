package com.safezone.infrastructure.data.repository;

import com.safezone.domain.enums.Role;
import com.safezone.domain.enums.UserStatus;
import com.safezone.domain.model.User;
import com.safezone.infrastructure.data.DBHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgresUserRepository implements UserRepository {

    private final DBHelper dbHelper;

    public PostgresUserRepository(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public User findById(int userId) throws SQLException {

        String sql =
                "SELECT user_id, login, password_hash, status, role " +
                        "FROM users " +
                        "WHERE user_id = ?";

        Map<String, Object> row =
                dbHelper.getSingleRow(sql, userId);

        return row == null ? null : mapUser(row);
    }

    @Override
    public User findByLogin(String login) throws SQLException {

        String sql =
                "SELECT user_id, login, password_hash, status, role " +
                        "FROM users " +
                        "WHERE login = ?";

        Map<String, Object> row =
                dbHelper.getSingleRow(sql, login);

        return row == null ? null : mapUser(row);
    }

    @Override
    public List<User> findAll() throws SQLException {

        String sql =
                "SELECT user_id, login, password_hash, status, role " +
                        "FROM users " +
                        "ORDER BY user_id";

        List<Map<String, Object>> rows =
                dbHelper.getDataFromDB(sql);

        List<User> users = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            users.add(mapUser(row));
        }

        return users;
    }

    @Override
    public boolean existsByLogin(String login)
            throws SQLException {

        return findByLogin(login) != null;
    }

    @Override
    public int create(
            String login,
            String passwordHash) throws SQLException {

        String sql =
                "INSERT INTO users " +
                        "(login, password_hash, status, role) " +
                        "VALUES (?, ?, 'ACTIVE', 'CLIENT') " +
                        "RETURNING user_id";

        return dbHelper.executeInsertReturning(
                sql,
                login,
                passwordHash
        );
    }

    @Override
    public void updateStatus(
            int userId,
            String status) throws SQLException {

        String sql =
                "UPDATE users " +
                        "SET status = ? " +
                        "WHERE user_id = ?";

        dbHelper.executeUpdateData(
                sql,
                status,
                userId
        );
    }

    private User mapUser(Map<String, Object> row) {

        int userId =
                ((Number) row.get("user_id")).intValue();

        String login =
                (String) row.get("login");

        String passwordHash =
                (String) row.get("password_hash");

        UserStatus status =
                UserStatus.valueOf(
                        ((String) row.get("status")).toUpperCase()
                );

        Role role =
                Role.valueOf(
                        ((String) row.get("role")).toUpperCase()
                );

        return new User(
                userId,
                login,
                passwordHash,
                status,
                role
        );
    }
}