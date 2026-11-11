package com.safezone.backend.api.dto.response;

import com.safezone.backend.domain.model.Warehouse;

public record WarehouseResponse(int id, String name, String addr) {
    public static WarehouseResponse from (Warehouse w) {
        return new WarehouseResponse(w.getWarehouseId(), w.getName(), w.getAddress());
    }
}
