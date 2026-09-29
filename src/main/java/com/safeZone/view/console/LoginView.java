package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.User;

import java.util.Scanner;

public class LoginView {

    private final SafeZoneService service;
    private final ConsoleInput input;

    public LoginView(
            SafeZoneService service,
            Scanner scanner) {

        this.service = service;
        this.input = new ConsoleInput(scanner);
    }

    public User login() {

        System.out.println("=================================");
        System.out.println("          АВТОРИЗАЦИЯ");
        System.out.println("=================================");
        System.out.println();

        String login =
                input.readString("Логин: ");

        String password =
                input.readString("Пароль: ");

        User user =
                service.findUserByLogin(login);

        if (user == null) {

            System.out.println();
            System.out.println(
                    "Пользователь не найден."
            );

            return null;
        }

        if (!user.getPasswordHash().equals(password)) {

            System.out.println();
            System.out.println(
                    "Неверный пароль."
            );

            return null;
        }

        if (user.getStatus().name().equals("BLOCKED")) {

            System.out.println();
            System.out.println(
                    "Пользователь заблокирован."
            );

            return null;
        }

        if (user.getStatus().name().equals("DELETED")) {

            System.out.println();
            System.out.println(
                    "Пользователь удалён."
            );

            return null;
        }

        System.out.println();
        System.out.println(
                "Добро пожаловать, "
                        + user.getLogin()
                        + "!"
        );

        return user;
    }
}