package com.safezone.application.service;

import com.safezone.domain.model.Warehouse;
import com.safezone.infrastructure.data.repository.WarehouseRepository;


import java.sql.SQLException;
import java.util.List;

public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<Warehouse> getWarehouses() {

        try {
            return warehouseRepository.findAll();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить склады.",
                    e
            );
        }
    }

    public Warehouse getWarehouseById(int warehouseId) {
        try {
            return warehouseRepository.findById(warehouseId);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось найти склад по ID.",
                    e
            );
        }
    }

}
