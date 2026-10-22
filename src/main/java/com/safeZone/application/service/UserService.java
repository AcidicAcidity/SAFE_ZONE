package com.safezone.application.service;

import com.safezone.application.exception.UserNotFoundException;
import com.safezone.domain.enums.UserStatus;
import com.safezone.domain.model.User;
import com.safezone.infrastructure.data.repository.UserRepository;


import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> getUsers() {

        try {
            return userRepository.findAll();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить пользователей.",
                    e
            );
        }
    }

    public User findUserById(int userId) {

        try {
            return userRepository.findById(userId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка поиска пользователя.",
                    e
            );
        }
    }

    public void blockUser(int userId) {
        updateUserStatus(
                userId,
                UserStatus.BLOCKED
        );
    }

    public void unblockUser(int userId) {
        updateUserStatus(
                userId,
                UserStatus.ACTIVE
        );
    }

    public void deleteUser(int userId) {
        updateUserStatus(
                userId,
                UserStatus.DELETED
        );
    }

    private void updateUserStatus(
            int userId,
            UserStatus status) {

        try {
            User user =
                    userRepository.findById(userId);

            if (user == null) {
                throw new UserNotFoundException(
                        "Пользователь не найден."
                );
            }

            userRepository.updateStatus(
                    userId,
                    status.name()
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка изменения пользователя.",
                    e
            );
        }
    }


}
