package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.User;

public class RegistrationView {

    private final SafeZoneService service;
    private final ConsoleInput input;

    public RegistrationView(
            SafeZoneService service,
            ConsoleInput input) {

        this.service = service;
        this.input = input;
    }

    public User register() {

        System.out.println();
        System.out.println("=================================");
        System.out.println("          РЕГИСТРАЦИЯ");
        System.out.println("=================================");
        System.out.println();

        String login =
                input.readString("Введите логин: ");

        String password =
                input.readString("Введите пароль: ");

        String passwordRepeat =
                input.readString(
                        "Повторите пароль: "
                );

        if (!password.equals(passwordRepeat)) {
            System.out.println(
                    "Пароли не совпадают."
            );
            return null;
        }

        try {

            User user =
                    service.register(
                            login,
                            password
                    );

            System.out.println();
            System.out.println(
                    "Регистрация успешно завершена."
            );
            System.out.println(
                    "Ваш ID: " + user.getUserId()
            );

            return user;

        } catch (RuntimeException e) {

            System.out.println();
            System.out.println(
                    "Ошибка: " + e.getMessage()
            );

            return null;
        }
    }
}