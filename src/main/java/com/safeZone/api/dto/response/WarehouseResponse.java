package com.safezone.api.dto.response;

import com.safezone.domain.model.Warehouse;

public record WarehouseResponse(int id, String name, String addr) {
    public static WarehouseResponse from (Warehouse w) {
        return new WarehouseResponse(w.getWarehouseId(), w.getName(), w.getAddress());
    }
}
