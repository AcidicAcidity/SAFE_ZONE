package com.safezone.view.console;

import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Warehouse;

import java.util.List;

public class CellView {

    private final SafeZoneService service;

    public CellView(
            SafeZoneService service) {

        this.service = service;
    }

    public void showAllCells() {

        System.out.println(
                "================================="
        );

        System.out.println(
                "          ЯЧЕЙКИ СКЛАДОВ"
        );

        System.out.println(
                "================================="
        );

        System.out.println();

        List<Warehouse> warehouses =
                service.getWarehouses();

        for (Warehouse warehouse : warehouses) {

            System.out.println(
                    "Склад №"
                            + warehouse.getWarehouseId()
            );

            System.out.println();

            showWarehouseScheme(
                    warehouse.getWarehouseId()
            );

            System.out.println();

            System.out.println(
                    "S — свободная SMALL"
            );

            System.out.println(
                    "s — занятая SMALL"
            );

            System.out.println(
                    "M — свободная MEDIUM"
            );

            System.out.println(
                    "m — занятая MEDIUM"
            );

            System.out.println(
                    "L — свободная LARGE"
            );

            System.out.println(
                    "l — занятая LARGE"
            );

            System.out.println();
        }
    }

    private void showWarehouseScheme(
            int warehouseId) {

        System.out.println(
                "┌───────────────┬───────────────┬───────────────┬───────────────┐"
        );

        showSmallRow(warehouseId);

        showMediumRow(warehouseId);

        showMediumRow(warehouseId);

        showLargeRow(warehouseId);

        showLargeRow(warehouseId);

        System.out.println(
                "└───────────────┴───────────────┴───────────────┴───────────────┘"
        );
    }

    private void showSmallRow(
            int warehouseId) {

        System.out.print("│ ");

        for (int block = 0; block < 4; block++) {

            int columnStart =
                    block * 4 + 1;

            for (int i = 0; i < 4; i++) {

                int column =
                        columnStart + i;

                Cell cell =
                        findCell(
                                warehouseId,
                                1,
                                column
                        );

                System.out.print(
                        getSymbol(cell, 'S')
                );

                if (i < 3) {
                    System.out.print(" ");
                }
            }

            System.out.print("       ");

            if (block < 3) {
                System.out.print("│ ");
            }
        }

        System.out.println("│");
    }

    private void showMediumRow(
            int warehouseId) {

        System.out.print("│ ");

        for (int block = 0; block < 4; block++) {

            int columnStart =
                    block * 4 + 1;

            Cell left =
                    findCell(
                            warehouseId,
                            2,
                            columnStart
                    );

            Cell right =
                    findCell(
                            warehouseId,
                            2,
                            columnStart + 2
                    );

            System.out.print(
                    getSymbol(left, 'M')
            );

            System.out.print("   ");

            System.out.print(
                    getSymbol(right, 'M')
            );

            System.out.print("         ");

            if (block < 3) {
                System.out.print("│ ");
            }
        }

        System.out.println("│");
    }

    private void showLargeRow(
            int warehouseId) {

        System.out.print("│ ");

        for (int block = 0; block < 4; block++) {

            int columnStart =
                    block * 4 + 1;

            Cell cell =
                    findCell(
                            warehouseId,
                            4,
                            columnStart
                    );

            char symbol =
                    getSymbol(cell, 'L');

            System.out.print(
                    ""
                            + symbol
                            + symbol
                            + symbol
                            + symbol
                            + symbol
            );

            System.out.print("         ");

            if (block < 3) {
                System.out.print("│ ");
            }
        }

        System.out.println("│");
    }

    private Cell findCell(
            int warehouseId,
            int row,
            int column) {

        for (Cell cell : service.getCells()) {

            if (cell.getWarehouseId()
                    == warehouseId
                    && cell.getRow() == row
                    && cell.getColumn() == column) {

                return cell;
            }
        }

        return null;
    }

    private char getSymbol(
            Cell cell,
            char freeSymbol) {

        if (cell == null) {
            return ' ';
        }

        if (cell.getStatus()
                == CellStatus.RENTED) {

            return Character.toLowerCase(
                    freeSymbol
            );
        }

        return freeSymbol;
    }
}