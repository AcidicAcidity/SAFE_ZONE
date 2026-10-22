package com.safezone.infrastructure.data.repository;

import com.safezone.domain.model.Cell;

import java.sql.SQLException;
import java.util.List;

public interface CellRepository {

    Cell findById(int cellId) throws SQLException;

    List<Cell> findAll() throws SQLException;

    List<Cell> findAvailable() throws SQLException;

    List<Cell> findByWarehouseId(int warehouseId) throws SQLException;

    int createCell(int warehouseId, int row, int column, String size) throws SQLException;
}
