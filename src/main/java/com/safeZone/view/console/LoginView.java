package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.User;

import java.util.Scanner;

public class LoginView {

    private final SafeZoneService service;
    private final ConsoleInput input;
    private final RegistrationView registrationView;

    public LoginView(
            SafeZoneService service,
            Scanner scanner) {

        this.service = service;
        this.input = new ConsoleInput(scanner);

        this.registrationView =
                new RegistrationView(
                        service,
                        input
                );
    }

    public User login() {

        while (true) {

            System.out.println();
            System.out.println("=================================");
            System.out.println("             SAFEZONE");
            System.out.println("=================================");
            System.out.println();
            System.out.println("1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("0. Завершить программу");
            System.out.println();

            String choice =
                    input.readString("Ваш выбор: ");

            switch (choice) {

                case "1":

                    User user = performLogin();

                    if (user != null) {
                        return user;
                    }

                    break;

                case "2":

                    registrationView.register();

                    break;

                case "0":

                    return null;

                default:

                    System.out.println(
                            "Неверный пункт меню."
                    );
            }
        }
    }

    private User performLogin() {

        System.out.println();
        System.out.println("=== ВХОД ===");

        String login =
                input.readString("Логин: ");

        String password =
                input.readString("Пароль: ");

        try {

            User user =
                    service.login(
                            login,
                            password
                    );

            System.out.println();
            System.out.println(
                    "Добро пожаловать, "
                            + user.getLogin()
                            + "!"
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