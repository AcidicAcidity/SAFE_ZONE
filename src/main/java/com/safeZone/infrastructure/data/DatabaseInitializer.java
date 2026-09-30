package com.safezone.infrastructure.data;

import com.safezone.application.service.PasswordService;
import com.safezone.domain.enums.CellSize;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class DatabaseInitializer {

    private final DBHelper dbHelper;

    public DatabaseInitializer(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void initialize() {

        try {
            initializeAdmin();
            initializeWarehouse();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Ошибка при инициализации базы данных",
                    e
            );
        }
    }

    private void initializeAdmin()
            throws SQLException {

        Properties env =
                envRead.readEnv();

        String adminLogin =
                env.getProperty("ADMIN_LOGIN");

        String adminPassword =
                env.getProperty("ADMIN_PASSWORD");

        if (adminLogin == null
                || adminLogin.isBlank()
                || adminPassword == null
                || adminPassword.isBlank()) {

            throw new IllegalStateException(
                    "ADMIN_LOGIN или ADMIN_PASSWORD не указаны в .env"
            );
        }

        Map<String, Object> admin =
                dbHelper.getSingleRow(
                        "SELECT user_id " +
                                "FROM users " +
                                "WHERE login = ?",
                        adminLogin
                );

        if (admin != null) {
            return;
        }

        PasswordService passwordService = new PasswordService();

        String passwordHash =
                passwordService.hash(
                        adminPassword
                );

        dbHelper.executeUpdateData(
                "INSERT INTO users " +
                        "(login, password_hash, status, role) " +
                        "VALUES (?, ?, 'ACTIVE', 'ADMIN')",
                adminLogin,
                passwordHash
        );

        System.out.println(
                "Создан администратор: "
                        + adminLogin
        );
    }

    private void initializeWarehouse()
            throws SQLException {

        Map<String, Object> warehouse =
                dbHelper.getSingleRow(
                        "SELECT warehouse_id FROM warehouses LIMIT 1"
                );

        if (warehouse != null) {
            return;
        }

        int warehouseId =
                dbHelper.executeInsertReturning(
                        "INSERT INTO warehouses (name, address) " +
                                "VALUES (?, ?) RETURNING warehouse_id",
                        "Склад Владимир",
                        "Адрес 1"
                );

        initializeCells(warehouseId);

        System.out.println(
                "Создан тестовый склад: Склад 1"
        );

        System.out.println(
                "Создано 28 ячеек."
        );
    }

    private void initializeCells(
            int warehouseId) throws SQLException {

        for (int block = 0; block < 4; block++) {

            int columnStart =
                    block * 4 + 1;

            createSmallCells(
                    warehouseId,
                    columnStart
            );

            createMediumCells(
                    warehouseId,
                    columnStart
            );

            createLargeCell(
                    warehouseId,
                    columnStart
            );
        }
    }

    private void createSmallCells(
            int warehouseId,
            int columnStart
    ) throws SQLException {

        for (int i = 0; i < 4; i++) {

            int column =
                    columnStart + i;

            createCell(
                    warehouseId,
                    1,
                    column,
                    "S-" + warehouseId + "-" + column,
                    CellSize.SMALL
            );
        }
    }

    private void createMediumCells(
            int warehouseId,
            int columnStart
    ) throws SQLException {

        createCell(
                warehouseId,
                2,
                columnStart,
                "M-" + warehouseId + "-" + columnStart,
                CellSize.MEDIUM
        );

        createCell(
                warehouseId,
                2,
                columnStart + 2,
                "M-" + warehouseId + "-" + (columnStart + 2),
                CellSize.MEDIUM
        );
    }

    private void createLargeCell(
            int warehouseId,
            int columnStart
    ) throws SQLException {

        createCell(
                warehouseId,
                4,
                columnStart,
                "L-" + warehouseId + "-" + columnStart,
                CellSize.LARGE
        );
    }

    private void createCell(
            int warehouseId,
            int row,
            int column,
            String number,
            CellSize size
    ) throws SQLException {

        dbHelper.executeUpdateData(
                "INSERT INTO cells " +
                        "(warehouse_id, row_num, column_num, number, size, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'AVAILABLE')",
                warehouseId,
                row,
                column,
                number,
                size.name()
        );
    }
}