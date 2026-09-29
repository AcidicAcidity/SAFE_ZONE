package com.safezone.infrastructure.data.repository;

import com.safezone.domain.model.Warehouse;

import java.sql.SQLException;
import java.util.List;

public interface WarehouseRepository {

    Warehouse findById(int warehouseId) throws SQLException;

    List<Warehouse> findAll() throws SQLException;
}