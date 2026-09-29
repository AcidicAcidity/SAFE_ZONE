package com.safezone.infrastructure.data.repository;

import com.safezone.domain.enums.CellSize;
import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.model.Cell;
import com.safezone.infrastructure.data.DBHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgresCellRepository implements CellRepository {

    private final DBHelper dbHelper;

    public PostgresCellRepository(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Cell findById(int cellId) throws SQLException {

        String sql =
                "SELECT cell_id, warehouse_id, row_num, " +
                        "column_num, number, size, status " +
                        "FROM cells " +
                        "WHERE cell_id = ?";

        Map<String, Object> row =
                dbHelper.getSingleRow(sql, cellId);

        if (row == null) {
            return null;
        }

        return mapCell(row);
    }

    @Override
    public List<Cell> findAll() throws SQLException {

        String sql =
                "SELECT cell_id, warehouse_id, row_num, " +
                        "column_num, number, size, status " +
                        "FROM cells " +
                        "ORDER BY warehouse_id, row_num, column_num";

        List<Map<String, Object>> rows =
                dbHelper.getDataFromDB(sql);

        List<Cell> cells =
                new ArrayList<>();

        for (Map<String, Object> row : rows) {
            cells.add(mapCell(row));
        }

        return cells;
    }

    @Override
    public List<Cell> findAvailable() throws SQLException {

        String sql =
                "SELECT cell_id, warehouse_id, row_num, " +
                        "column_num, number, size, status " +
                        "FROM cells " +
                        "WHERE status = 'AVAILABLE' " +
                        "ORDER BY warehouse_id, row_num, column_num";

        List<Map<String, Object>> rows =
                dbHelper.getDataFromDB(sql);

        List<Cell> cells =
                new ArrayList<>();

        for (Map<String, Object> row : rows) {
            cells.add(mapCell(row));
        }

        return cells;
    }

    private Cell mapCell(
            Map<String, Object> row) {

        int cellId =
                ((Number) row.get("cell_id"))
                        .intValue();

        int warehouseId =
                ((Number) row.get("warehouse_id"))
                        .intValue();

        int rowNumber =
                ((Number) row.get("row_num"))
                        .intValue();

        int column =
                ((Number) row.get("column_num"))
                        .intValue();

        String number =
                (String) row.get("number");

        CellSize size =
                CellSize.valueOf(
                        ((String) row.get("size"))
                                .toUpperCase()
                );

        CellStatus status =
                CellStatus.valueOf(
                        ((String) row.get("status"))
                                .toUpperCase()
                );

        return new Cell(
                cellId,
                warehouseId,
                rowNumber,
                column,
                number,
                size,
                status
        );
    }
}