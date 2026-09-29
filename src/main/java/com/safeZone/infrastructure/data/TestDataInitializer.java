package com.safezone.infrastructure.data;

import com.safezone.domain.enums.CellSize;
import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Warehouse;

import java.util.List;

public class TestDataInitializer {

    private int nextCellId = 1;

    public void initializeWarehouses(List<Warehouse> warehouses) {
        warehouses.add(new Warehouse(
                1,
                "Постамат №1",
                "Адрес 1"
        ));

        warehouses.add(new Warehouse(
                2,
                "Постамат №2",
                "Адрес 2"
        ));
    }

    public void initializeCells(
            List<Cell> cells,
            int warehouseId
    ) {
        for (int block = 0; block < 4; block++) {

            int columnStart = block * 4 + 1;

            createSmallCells(
                    cells,
                    warehouseId,
                    columnStart
            );

            createMediumCells(
                    cells,
                    warehouseId,
                    columnStart
            );

            createLargeCell(
                    cells,
                    warehouseId,
                    columnStart
            );
        }
    }

    private void createSmallCells(
            List<Cell> cells,
            int warehouseId,
            int columnStart
    ) {
        for (int i = 0; i < 4; i++) {

            int column = columnStart + i;

            cells.add(new Cell(
                    nextCellId++,
                    warehouseId,
                    1,
                    column,
                    "S-" + warehouseId + "-" + column,
                    CellSize.SMALL,
                    CellStatus.AVAILABLE
            ));
        }
    }

    private void createMediumCells(
            List<Cell> cells,
            int warehouseId,
            int columnStart
    ) {
        cells.add(new Cell(
                nextCellId++,
                warehouseId,
                2,
                columnStart,
                "M-" + warehouseId + "-" + columnStart,
                CellSize.MEDIUM,
                CellStatus.AVAILABLE
        ));

        cells.add(new Cell(
                nextCellId++,
                warehouseId,
                2,
                columnStart + 2,
                "M-" + warehouseId + "-" + (columnStart + 2),
                CellSize.MEDIUM,
                CellStatus.AVAILABLE
        ));
    }

    private void createLargeCell(
            List<Cell> cells,
            int warehouseId,
            int columnStart
    ) {
        cells.add(new Cell(
                nextCellId++,
                warehouseId,
                4,
                columnStart,
                "L-" + warehouseId + "-" + columnStart,
                CellSize.LARGE,
                CellStatus.AVAILABLE
        ));
    }
}