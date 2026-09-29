package com.safezone.view.console;

import com.safezone.domain.enums.CellSize;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ConsoleInput {

    private final Scanner scanner;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readString(String message) {

        System.out.print(message);

        return scanner.nextLine();
    }

    public int readInt(String message) {

        while (true) {

            System.out.print(message);

            try {
                return Integer.parseInt(
                        scanner.nextLine()
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Введите целое число."
                );
            }
        }
    }

    public int readRentalHours() {

        while (true) {

            int hours = readInt(
                    "Введите продолжительность аренды (1-24 часа): "
            );

            if (hours >= 1 && hours <= 24) {
                return hours;
            }

            System.out.println(
                    "Продолжительность должна быть от 1 до 24 часов."
            );
        }
    }

    public LocalDate readRentalDate() {

        while (true) {

            System.out.print(
                    "Введите дату (дд.мм.гггг): "
            );

            String input = scanner.nextLine();

            try {

                LocalDate date =
                        LocalDate.parse(
                                input,
                                dateFormatter
                        );

                LocalDate today =
                        LocalDate.now();

                if (date.isBefore(today)) {

                    System.out.println(
                            "Нельзя выбрать дату в прошлом."
                    );

                    System.out.println(
                            "Сегодня: "
                                    + today.format(
                                    dateFormatter
                            )
                    );

                    continue;
                }

                return date;

            } catch (Exception e) {

                System.out.println(
                        "Некорректная дата."
                );

                System.out.println(
                        "Используйте формат: дд.мм.гггг"
                );
            }
        }
    }

    public CellSize readCellSize() {

        while (true) {

            System.out.println();
            System.out.println("Выберите размер:");
            System.out.println("1. SMALL");
            System.out.println("2. MEDIUM");
            System.out.println("3. LARGE");
            System.out.println("0. Назад");

            String choice =
                    readString("Ваш выбор: ");

            switch (choice) {

                case "1":
                    return CellSize.SMALL;

                case "2":
                    return CellSize.MEDIUM;

                case "3":
                    return CellSize.LARGE;

                case "0":
                    return null;

                default:
                    System.out.println(
                            "Неверный выбор."
                    );
                    break;
            }
        }
    }

    public void waitForEnter() {

        System.out.println();
        System.out.println(
                "Нажмите Enter для возврата в меню..."
        );

        scanner.nextLine();
    }
}