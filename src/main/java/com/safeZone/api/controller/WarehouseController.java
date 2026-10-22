package com.safezone.api.controller;

import com.safezone.api.dto.WarehouseResponse;
import com.safezone.application.service.SafeZoneService;
import com.safezone.domain.model.Warehouse;
import com.safezone.api.exception.NotFoundException;

import java.sql.SQLException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

    private final SafeZoneService service;

    public WarehouseController(SafeZoneService service) {
        this.service = service;
    }

    @GetMapping
    public List<WarehouseResponse> getAll() throws SQLException {
        return service.getWarehouses().stream()
            .map(WarehouseResponse::from)
            .toList();
    }

    @GetMapping("/{warehouseId}")
    public WarehouseResponse getById(@PathVariable int warehouseId) throws SQLException {
        Warehouse w = service.getWarehouseById(warehouseId);
        if (w == null) {
            throw new NotFoundException("Warehouse" + warehouseId + " not found");
        }
        return WarehouseResponse.from(w);
    }
}
