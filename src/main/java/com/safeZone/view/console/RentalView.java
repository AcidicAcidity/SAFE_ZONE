package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.enums.CellSize;
import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Rental;
import com.safezone.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class RentalView {

    private final SafeZoneService service;
    private final ConsoleInput input;

    public RentalView(
            SafeZoneService service,
            Scanner scanner) {

        this.service = service;
        this.input = new ConsoleInput(scanner);
    }

    public void rentCell(User currentUser) {

        System.out.println(
                "================================="
        );

        System.out.println(
                "              АРЕНДА"
        );

        System.out.println(
                "================================="
        );

        System.out.println();

        System.out.println(
                "1. Выбрать конкретную ячейку"
        );

        System.out.println(
                "2. Выбрать ячейку по размеру"
        );

        System.out.println(
                "0. Назад"
        );

        System.out.println();

        String choice =
                input.readString(
                        "Выберите вариант: "
                );

        switch (choice) {

            case "1":
                rentSpecificCell(currentUser);
                break;

            case "2":
                rentCellBySize(currentUser);
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

    private void rentSpecificCell(User currentUser) {

        System.out.println();
        System.out.println(
                "=== Выбор конкретной ячейки ==="
        );
        System.out.println();

        System.out.println("Доступные ячейки:");
        System.out.println();

        for (Cell cell : service.getCells()) {

            if (cell.getStatus() == CellStatus.AVAILABLE) {

                System.out.println(
                        "ID: "
                                + cell.getCellId()
                                + " | Склад: "
                                + cell.getWarehouseId()
                                + " | Ячейка: "
                                + cell.getNumber()
                                + " | Размер: "
                                + cell.getSize()
                );
            }
        }

        System.out.println();

        int cellId =
                input.readInt(
                        "Введите ID ячейки: "
                );

        Cell cell =
                service.findCellById(cellId);

        if (cell == null) {

            System.out.println(
                    "Ячейка не найдена."
            );

            return;
        }

        if (cell.getStatus()
                != CellStatus.AVAILABLE) {

            System.out.println(
                    "Эта ячейка недоступна."
            );

            return;
        }

        System.out.println();

        System.out.println(
                "Ячейка: "
                        + cell.getNumber()
        );

        System.out.println(
                "Размер: "
                        + cell.getSize()
        );

        System.out.println();

        LocalDateTime startDateTime =
                input.readRentalStartDateTime();

        int hours =
                input.readRentalHours();

        try {

            Rental rental =
                    service.rentSpecificCell(
                            currentUser.getUserId(),
                            cellId,
                            startDateTime,
                            hours
                    );

            printRentalResult(rental);

        } catch (IllegalArgumentException e) {

            System.out.println(
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            System.out.println(
                    e.getMessage()
            );
        }
    }
    private void rentCellBySize(User currentUser) {

        System.out.println();
        System.out.println(
                "=== Выбор ячейки по размеру ==="
        );

        CellSize size =
                input.readCellSize();

        if (size == null) {
            return;
        }

        System.out.println();

        LocalDateTime startDateTime =
                input.readRentalStartDateTime();

        int hours =
                input.readRentalHours();

        try {

            Rental rental =
                    service.rentCellBySize(
                            currentUser.getUserId(),
                            size,
                            startDateTime,
                            hours
                    );

            printRentalResult(rental);

        } catch (IllegalArgumentException e) {

            System.out.println(
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            System.out.println(
                    e.getMessage()
            );
        }
    }

    private void printRentalResult(
            Rental rental) {

        Cell cell =
                service.findCellById(
                        rental.getCellId()
                );

        System.out.println();

        System.out.println(
                "================================="
        );

        System.out.println(
                "          АРЕНДА СОЗДАНА"
        );

        System.out.println(
                "================================="
        );

        System.out.println();

        System.out.println(
                "ID аренды: "
                        + rental.getRentalId()
        );

        System.out.println(
                "Склад: "
                        + cell.getWarehouseId()
        );

        System.out.println(
                "Ячейка: "
                        + cell.getNumber()
        );

        System.out.println(
                "Размер: "
                        + cell.getSize()
        );

        System.out.println(
                "Начало: "
                        + rental.getStartDateTime()
        );

        System.out.println(
                "Окончание: "
                        + rental.getEndDateTime()
        );

        System.out.println(
                "Статус: "
                        + rental.getStatus()
        );
    }

    public void showMyRentals(User currentUser) {

        System.out.println(
                "================================="
        );

        System.out.println(
                "           МОИ АРЕНДЫ"
        );

        System.out.println(
                "================================="
        );

        System.out.println();

        List<Rental> rentals =
                service.getUserRentals(
                        currentUser.getUserId()
                );

        if (rentals.isEmpty()) {

            System.out.println(
                    "У вас нет аренд."
            );

            return;
        }

        for (Rental rental : rentals) {

            Cell cell =
                    service.findCellById(
                            rental.getCellId()
                    );

            System.out.println(
                    "Аренда №"
                            + rental.getRentalId()
            );

            System.out.println(
                    "Склад: "
                            + cell.getWarehouseId()
            );

            System.out.println(
                    "Ячейка: "
                            + cell.getNumber()
            );

            System.out.println(
                    "Размер: "
                            + cell.getSize()
            );

            System.out.println(
                    "Начало: "
                            + rental.getStartDateTime()
            );

            System.out.println(
                    "Окончание: "
                            + rental.getEndDateTime()
            );

            System.out.println(
                    "Статус: "
                            + rental.getStatus()
            );

            System.out.println(
                    "---------------------------------"
            );
        }
    }
}