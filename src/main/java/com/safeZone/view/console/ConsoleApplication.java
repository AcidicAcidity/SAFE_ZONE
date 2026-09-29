package com.safezone.view.console;

import com.safezone.application.service.PasswordService;
import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.User;
import com.safezone.infrastructure.data.DBHelper;
import com.safezone.infrastructure.data.repository.CellRepository;
import com.safezone.infrastructure.data.repository.PostgresCellRepository;
import com.safezone.infrastructure.data.repository.PostgresRentalRepository;
import com.safezone.infrastructure.data.repository.PostgresUserRepository;
import com.safezone.infrastructure.data.repository.PostgresWarehouseRepository;
import com.safezone.infrastructure.data.repository.RentalRepository;
import com.safezone.infrastructure.data.repository.UserRepository;
import com.safezone.infrastructure.data.repository.WarehouseRepository;

import java.util.Scanner;

public class ConsoleApplication {

    private final SafeZoneService service;
    private final Scanner scanner;

    private final LoginView loginView;
    private final ConsoleMenu consoleMenu;

    public ConsoleApplication() {

        /*
         * =========================================
         * DATABASE
         * =========================================
         */

        DBHelper dbHelper = new DBHelper();


        /*
         * =========================================
         * REPOSITORIES
         * =========================================
         */

        UserRepository userRepository =
                new PostgresUserRepository(
                        dbHelper
                );

        WarehouseRepository warehouseRepository =
                new PostgresWarehouseRepository(
                        dbHelper
                );

        CellRepository cellRepository =
                new PostgresCellRepository(
                        dbHelper
                );

        RentalRepository rentalRepository =
                new PostgresRentalRepository(
                        dbHelper
                );


        /*
         * =========================================
         * SERVICES
         * =========================================
         */

        PasswordService passwordService =
                new PasswordService();

        service =
                new SafeZoneService(
                        userRepository,
                        warehouseRepository,
                        cellRepository,
                        rentalRepository,
                        passwordService
                );


        /*
         * =========================================
         * CONSOLE
         * =========================================
         */

        scanner = new Scanner(System.in);

        loginView =
                new LoginView(
                        service,
                        scanner
                );

        consoleMenu =
                new ConsoleMenu(
                        service,
                        scanner
                );
    }

    public void start() {

        boolean applicationRunning = true;

        while (applicationRunning) {

            /*
             * Показываем экран авторизации.
             *
             * Если пользователь выбрал "0",
             * login() вернёт null.
             */
            User currentUser =
                    loginView.login();

            if (currentUser == null) {
                applicationRunning = false;
                continue;
            }


            /*
             * Пользователь успешно вошёл.
             *
             * ConsoleMenu.start() возвращает:
             *
             * true  -> пользователь вышел
             *         из аккаунта;
             *
             * false -> приложение нужно завершить.
             */
            boolean logout =
                    consoleMenu.start(
                            currentUser
                    );

            if (!logout) {
                applicationRunning = false;
            }
        }

        scanner.close();

        System.out.println();
        System.out.println(
                "================================="
        );
        System.out.println(
                "       SAFEZONE завершён"
        );
        System.out.println(
                "================================="
        );
    }
}