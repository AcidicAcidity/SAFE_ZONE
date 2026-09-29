package com.safezone.domain.model;

import com.safezone.domain.enums.CellSize;
import com.safezone.domain.enums.CellStatus;

public class Cell {

    private final int cellId;
    private final int warehouseId;

    private final int row;
    private final int column;

    private final String number;
    private final CellSize size;

    private CellStatus status;

    public Cell(
            int cellId,
            int warehouseId,
            int row,
            int column,
            String number,
            CellSize size,
            CellStatus status
    ) {
        this.cellId = cellId;
        this.warehouseId = warehouseId;
        this.row = row;
        this.column = column;
        this.number = number;
        this.size = size;
        this.status = status;
    }

    public int getCellId() {
        return cellId;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getNumber() {
        return number;
    }

    public CellSize getSize() {
        return size;
    }

    public CellStatus getStatus() {
        return status;
    }

    public void setStatus(CellStatus status) {
        this.status = status;
    }
}