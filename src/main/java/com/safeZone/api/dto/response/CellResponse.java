package com.safezone.api.dto.response;

import com.safezone.domain.model.Cell;

public record CellResponse(int id, int warehouseId, int row, int column, String number, String size, String status) {
    public static CellResponse from (Cell c) {
        return new CellResponse(c.getCellId(), c.getWarehouseId(), c.getRow(), c.getColumn(), c.getNumber(), c.getSize().name(), c.getStatus().name());
    }
}
