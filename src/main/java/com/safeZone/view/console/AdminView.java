package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Rental;
import com.safezone.domain.model.User;

import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import com.safezone.infrastructure.data.envRead;
import com.safezone.application.service.ExportDB;

public class AdminView {

    private final Properties env = envRead.readEnv();

    private final String url = env.getProperty("DB_URL");
    private final String user = env.getProperty("DB_USER");
    private final String password = env.getProperty("DB_PASSWORD");

    private final SafeZoneService service;
    private final ConsoleInput input;

    public AdminView(
            SafeZoneService service,
            Scanner scanner) {

        this.service = service;
        this.input = new ConsoleInput(scanner);
    }

    public void start() {

        while (true) {

            System.out.println();
            System.out.println("=================================");
            System.out.println("      ПАНЕЛЬ АДМИНИСТРАТОРА");
            System.out.println("=================================");
            System.out.println();

            System.out.println("1. Статистика по ячейкам");
            System.out.println("2. Управление аккаунтами");
            System.out.println("3. Управление бронями");
            System.out.println("4. Иморт базы данных в xlsx");
            System.out.println("0. Назад");

            System.out.println();

            String choice =
                    input.readString("Выберите действие: ");

            switch (choice) {

                case "1":
                    showCellStatistics();
                    break;

                case "2":
                    manageUsers();
                    break;

                case "3":
                    manageRentals();
                    break;

                case "4":
                    try {
                        ExportDB.exportDatabaseToXlsx(url, user, password);
                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "0":
                    return;

                default:
                    System.out.println(
                            "Неверный выбор."
                    );
                    break;
            }
        }
    }

    private void showCellStatistics() {

        System.out.println();
        System.out.println("=================================");
        System.out.println("       СТАТИСТИКА ПО ЯЧЕЙКАМ");
        System.out.println("=================================");
        System.out.println();

        for (Cell cell : service.getCells()) {

            int rentalCount =
                    service.getCellRentalCount(
                            cell.getCellId()
                    );

            System.out.println(
                    "Ячейка: "
                            + cell.getNumber()
                            + " | Размер: "
                            + cell.getSize()
                            + " | Аренд: "
                            + rentalCount
            );
        }

        input.waitForEnter();
    }

    private void manageUsers() {

        while (true) {

            System.out.println();
            System.out.println("=================================");
            System.out.println("       УПРАВЛЕНИЕ АККАУНТАМИ");
            System.out.println("=================================");
            System.out.println();

            showUsers();

            System.out.println();
            System.out.println("1. Заблокировать пользователя");
            System.out.println("2. Разблокировать пользователя");
            System.out.println("3. Удалить пользователя");
            System.out.println("0. Назад");

            System.out.println();

            String choice =
                    input.readString("Выберите действие: ");

            switch (choice) {

                case "1":
                    blockUser();
                    break;

                case "2":
                    unblockUser();
                    break;

                case "3":
                    deleteUser();
                    break;

                case "0":
                    return;

                default:
                    System.out.println(
                            "Неверный выбор."
                    );
                    break;
            }
        }
    }

    private void showUsers() {

        List<User> users =
                service.getUsers();

        if (users.isEmpty()) {
            System.out.println("Пользователей нет.");
            return;
        }

        for (User user : users) {

            System.out.println(
                    "ID: "
                            + user.getUserId()
                            + " | Логин: "
                            + user.getLogin()
                            + " | Роль: "
                            + user.getRole()
                            + " | Статус: "
                            + user.getStatus()
            );
        }
    }

    private void blockUser() {

        int userId =
                input.readInt(
                        "Введите ID пользователя: "
                );

        try {

            service.blockUser(userId);

            System.out.println(
                    "Пользователь заблокирован."
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        input.waitForEnter();
    }

    private void unblockUser() {

        int userId =
                input.readInt(
                        "Введите ID пользователя: "
                );

        try {

            service.unblockUser(userId);

            System.out.println(
                    "Пользователь разблокирован."
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        input.waitForEnter();
    }

    private void deleteUser() {

        int userId =
                input.readInt(
                        "Введите ID пользователя: "
                );

        try {

            service.deleteUser(userId);

            System.out.println(
                    "Пользователь удалён."
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        input.waitForEnter();
    }

    private void manageRentals() {

        while (true) {

            System.out.println();
            System.out.println("=================================");
            System.out.println("        УПРАВЛЕНИЕ БРОНЯМИ");
            System.out.println("=================================");
            System.out.println();

            showAllRentals();

            System.out.println();
            System.out.println("1. Отменить бронь");
            System.out.println("0. Назад");

            System.out.println();

            String choice =
                    input.readString("Выберите действие: ");

            switch (choice) {

                case "1":
                    cancelRental();
                    break;

                case "0":
                    return;

                default:
                    System.out.println(
                            "Неверный выбор."
                    );
                    break;
            }
        }
    }

    private void showAllRentals() {

        List<Rental> rentals =
                service.getRentals();

        if (rentals.isEmpty()) {

            System.out.println(
                    "Броней нет."
            );

            return;
        }

        for (Rental rental : rentals) {

            Cell cell =
                    service.findCellById(
                            rental.getCellId()
                    );

            System.out.println(
                    "Бронь №"
                            + rental.getRentalId()
                            + " | Пользователь: "
                            + rental.getUserId()
                            + " | Ячейка: "
                            + cell.getNumber()
                            + " | Начало: "
                            + rental.getStartDateTime()
                            + " | Окончание: "
                            + rental.getEndDateTime()
                            + " | Статус: "
                            + rental.getStatus()
            );
        }
    }

    private void cancelRental() {

        int rentalId =
                input.readInt(
                        "Введите ID брони: "
                );

        try {

            service.cancelRental(rentalId);

            System.out.println(
                    "Бронь отменена."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            System.out.println(
                    e.getMessage()
            );
        }

        input.waitForEnter();
    }
}
