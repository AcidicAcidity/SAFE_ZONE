package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.enums.Role;
import com.safezone.domain.model.User;

import java.util.Scanner;

public class ConsoleMenu {

    private final SafeZoneService service;
    private final ConsoleInput input;

    private final WarehouseView warehouseView;
    private final CellView cellView;
    private final RentalView rentalView;
    private final AdminView adminView;

    public ConsoleMenu(
            SafeZoneService service,
            Scanner scanner) {

        this.service = service;
        this.input = new ConsoleInput(scanner);

        warehouseView =
                new WarehouseView(service);

        cellView =
                new CellView(service);

        rentalView =
                new RentalView(service, scanner);

        adminView =
                new AdminView(service, scanner);
    }

    public void start(User currentUser) {

        boolean running = true;

        while (running) {

            clearScreen();

            showHeader(currentUser);
            showMenu(currentUser);

            String choice =
                    input.readString(
                            "Выберите действие: "
                    );

            switch (choice) {

                case "1":
                    clearScreen();

                    warehouseView.showWarehouses();

                    input.waitForEnter();
                    break;

                case "2":
                    clearScreen();

                    cellView.showAllCells();

                    input.waitForEnter();
                    break;

                case "3":
                    clearScreen();

                    rentalView.rentCell(currentUser);

                    input.waitForEnter();
                    break;

                case "4":
                    clearScreen();

                    rentalView.showMyRentals(currentUser);

                    input.waitForEnter();
                    break;

                case "5":

                    if (currentUser.getRole() == Role.ADMIN) {

                        clearScreen();

                        adminView.start();
                    }

                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println();
                    System.out.println(
                            "Неверный пункт меню."
                    );

                    input.waitForEnter();
                    break;
            }
        }

        clearScreen();

        System.out.println(
                "Работа приложения завершена."
        );
    }

    private void showHeader(User user) {

        System.out.println(
                "================================="
        );

        System.out.println(
                "             SAFEZONE"
        );

        System.out.println(
                "================================="
        );

        System.out.println(
                "Пользователь: "
                        + user.getLogin()
        );

        System.out.println(
                "Роль: "
                        + user.getRole()
        );

        System.out.println(
                "---------------------------------"
        );
    }

    private void showMenu(User currentUser) {

        System.out.println(
                "1. Показать склады"
        );

        System.out.println(
                "2. Показать все ячейки"
        );

        System.out.println(
                "3. Арендовать"
        );

        System.out.println(
                "4. Мои аренды"
        );

        if (currentUser.getRole() == Role.ADMIN) {

            System.out.println(
                    "5. Панель администратора"
            );
        }

        System.out.println(
                "0. Выход"
        );

        System.out.println(
                "---------------------------------"
        );
    }

    private void clearScreen() {

        System.out.print(
                "\033[H\033[2J"
        );

        System.out.flush();
    }
}