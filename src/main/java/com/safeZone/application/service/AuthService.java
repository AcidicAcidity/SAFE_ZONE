package com.safezone.application.service;

import com.safezone.application.exception.AuthenticationException;
import com.safezone.application.exception.UserAlreadyExistsException;
import com.safezone.domain.enums.UserStatus;
import com.safezone.domain.model.User;
import com.safezone.infrastructure.data.repository.UserRepository;


import java.sql.SQLException;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.passwordService = passwordService;
        this.userRepository = userRepository;
    }

    public User login(String login, String password) {

        try {
            User user = userRepository.findByLogin(login);

            if (user == null) {
                throw new AuthenticationException(
                        "Неверный логин или пароль."
                );
            }

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new AuthenticationException(
                        "Ваш аккаунт заблокирован."
                );
            }

            if (user.getStatus() == UserStatus.DELETED) {
                throw new AuthenticationException(
                        "Ваш аккаунт удалён."
                );
            }

            if (!passwordService.matches(
                    password,
                    user.getPasswordHash())) {

                throw new AuthenticationException(
                        "Неверный логин или пароль."
                );
            }

            return user;

        } catch (SQLException e) {

            e.printStackTrace();

            throw new IllegalStateException(
                    "Ошибка при обращении к базе данных: "
                            + e.getMessage(),
                    e
            );
        }
    }

    public User register(String login, String password) {

        validateRegistration(login, password);

        try {
            if (userRepository.existsByLogin(login)) {
                throw new UserAlreadyExistsException(
                        "Пользователь с таким логином уже существует."
                );
            }

            String passwordHash =
                    passwordService.hash(password);

            int userId =
                    userRepository.create(
                            login,
                            passwordHash
                    );

            return userRepository.findById(userId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка регистрации.",
                    e
            );
        }
    }

    private void validateRegistration(
            String login,
            String password) {

        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Логин не может быть пустым."
            );
        }

        if (login.length() < 3) {
            throw new IllegalArgumentException(
                    "Логин должен содержать минимум 3 символа."
            );
        }

        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать минимум 4 символа."
            );
        }
    }

}
