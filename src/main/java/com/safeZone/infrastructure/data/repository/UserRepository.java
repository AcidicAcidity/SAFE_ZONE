package com.safezone.infrastructure.data.repository;

import com.safezone.domain.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserRepository {

    User findById(int userId) throws SQLException;

    User findByLogin(String login) throws SQLException;

    List<User> findAll() throws SQLException;

    boolean existsByLogin(String login) throws SQLException;

    int create(
            String login,
            String passwordHash) throws SQLException;

    void updateStatus(
            int userId,
            String status) throws SQLException;
}