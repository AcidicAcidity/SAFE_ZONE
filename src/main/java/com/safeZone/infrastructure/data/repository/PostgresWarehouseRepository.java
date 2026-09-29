package com.safezone.infrastructure.data.repository;

import com.safezone.domain.model.Warehouse;
import com.safezone.infrastructure.data.DBHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgresWarehouseRepository implements WarehouseRepository {

    private final DBHelper dbHelper;

    public PostgresWarehouseRepository(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Warehouse findById(int warehouseId) throws SQLException {

        String sql =
                "SELECT warehouse_id, name, address " +
                        "FROM warehouses " +
                        "WHERE warehouse_id = ?";

        Map<String, Object> row =
                dbHelper.getSingleRow(sql, warehouseId);

        if (row == null) {
            return null;
        }

        return mapWarehouse(row);
    }

    @Override
    public List<Warehouse> findAll() throws SQLException {

        String sql =
                "SELECT warehouse_id, name, address " +
                        "FROM warehouses " +
                        "ORDER BY warehouse_id";

        List<Map<String, Object>> rows =
                dbHelper.getDataFromDB(sql);

        List<Warehouse> warehouses =
                new ArrayList<>();

        for (Map<String, Object> row : rows) {
            warehouses.add(mapWarehouse(row));
        }

        return warehouses;
    }

    private Warehouse mapWarehouse(
            Map<String, Object> row) {

        int warehouseId =
                ((Number) row.get("warehouse_id"))
                        .intValue();

        String name =
                (String) row.get("name");

        String address =
                (String) row.get("address");

        return new Warehouse(
                warehouseId,
                name,
                address
        );
    }
}