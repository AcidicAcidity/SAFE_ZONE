package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.User;

import java.util.Scanner;

public class ConsoleApplication {

    private final SafeZoneService service;
    private final Scanner scanner;

    private final LoginView loginView;
    private final ConsoleMenu consoleMenu;

    public ConsoleApplication() {

        service = new SafeZoneService();
        scanner = new Scanner(System.in);

        loginView = new LoginView(
                service,
                scanner
        );

        consoleMenu = new ConsoleMenu(
                service,
                scanner
        );
    }

    public void start() {

        User user = loginView.login();

        if (user == null) {
            return;
        }

        consoleMenu.start(user);
    }
}