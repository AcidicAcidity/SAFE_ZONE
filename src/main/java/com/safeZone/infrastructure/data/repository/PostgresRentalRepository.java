package com.safezone.infrastructure.data.repository;

import com.safezone.domain.enums.RentalStatus;
import com.safezone.domain.model.Rental;
import com.safezone.infrastructure.data.DBHelper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgresRentalRepository
        implements RentalRepository {

    private final DBHelper dbHelper;

    public PostgresRentalRepository(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public Rental findById(int rentalId)
            throws SQLException {

        String sql =
                "SELECT rental_id, user_id, cell_id, " +
                        "start_date_time, end_date_time, status " +
                        "FROM rentals " +
                        "WHERE rental_id = ?";

        Map<String, Object> row =
                dbHelper.getSingleRow(
                        sql,
                        rentalId
                );

        if (row == null) {
            return null;
        }

        return mapRental(row);
    }

    @Override
    public List<Rental> findAll()
            throws SQLException {

        String sql =
                "SELECT rental_id, user_id, cell_id, " +
                        "start_date_time, end_date_time, status " +
                        "FROM rentals " +
                        "ORDER BY rental_id";

        List<Map<String, Object>> rows =
                dbHelper.getDataFromDB(sql);

        List<Rental> rentals =
                new ArrayList<>();

        for (Map<String, Object> row : rows) {
            rentals.add(mapRental(row));
        }

        return rentals;
    }

    @Override
    public List<Rental> findByUserId(int userId)
            throws SQLException {

        String sql =
                "SELECT rental_id, user_id, cell_id, " +
                        "start_date_time, end_date_time, status " +
                        "FROM rentals " +
                        "WHERE user_id = ? " +
                        "ORDER BY start_date_time DESC";

        List<Map<String, Object>> rows =
                dbHelper.getDataFromDB(
                        sql,
                        userId
                );

        List<Rental> rentals =
                new ArrayList<>();

        for (Map<String, Object> row : rows) {
            rentals.add(mapRental(row));
        }

        return rentals;
    }

    @Override
    public int create(
            int userId,
            int cellId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime)
            throws SQLException {

        String sql =
                "INSERT INTO rentals " +
                        "(user_id, cell_id, start_date_time, " +
                        "end_date_time, status) " +
                        "VALUES (?, ?, ?, ?, 'ACTIVE') " +
                        "RETURNING rental_id";

        return dbHelper.executeInsertReturning(
                sql,
                userId,
                cellId,
                Timestamp.valueOf(startDateTime),
                Timestamp.valueOf(endDateTime)
        );
    }

    @Override
    public void updateStatus(
            int rentalId,
            RentalStatus status)
            throws SQLException {

        String sql =
                "UPDATE rentals " +
                        "SET status = ? " +
                        "WHERE rental_id = ?";

        dbHelper.executeUpdateData(
                sql,
                status.name(),
                rentalId
        );
    }

    @Override
    public int countByCellId(int cellId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "AS rental_count " +
                        "FROM rentals " +
                        "WHERE cell_id = ?";

        Map<String, Object> row =
                dbHelper.getSingleRow(
                        sql,
                        cellId
                );

        if (row == null) {
            return 0;
        }

        return ((Number) row.get("rental_count"))
                .intValue();
    }

    @Override
    public boolean hasActiveRental(
            int cellId
    ) throws SQLException {

        String sql =
                "SELECT rental_id " +
                        "FROM rentals " +
                        "WHERE cell_id = ? " +
                        "AND status = 'ACTIVE' " +
                        "AND start_date_time <= CURRENT_TIMESTAMP " +
                        "AND end_date_time > CURRENT_TIMESTAMP " +
                        "LIMIT 1";

        Map<String, Object> row =
                dbHelper.getSingleRow(
                        sql,
                        cellId
                );

        return row != null;
    }

    @Override
    public boolean hasOverlappingRental(
            int cellId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime)
            throws SQLException {

        String sql =
                "SELECT rental_id " +
                        "FROM rentals " +
                        "WHERE cell_id = ? " +
                        "AND status = 'ACTIVE' " +
                        "AND start_date_time < ? " +
                        "AND end_date_time > ? " +
                        "LIMIT 1";

        Map<String, Object> row =
                dbHelper.getSingleRow(
                        sql,
                        cellId,
                        Timestamp.valueOf(endDateTime),
                        Timestamp.valueOf(startDateTime)
                );

        return row != null;
    }

    @Override
    public void markExpiredRentals(
            LocalDateTime currentDateTime)
            throws SQLException {

        String sql =
                "UPDATE rentals " +
                        "SET status = 'EXPIRED' " +
                        "WHERE status = 'ACTIVE' " +
                        "AND end_date_time <= ?";

        dbHelper.executeUpdateData(
                sql,
                Timestamp.valueOf(currentDateTime)
        );
    }

    private Rental mapRental(
            Map<String, Object> row) {

        int rentalId =
                ((Number) row.get("rental_id"))
                        .intValue();

        int userId =
                ((Number) row.get("user_id"))
                        .intValue();

        int cellId =
                ((Number) row.get("cell_id"))
                        .intValue();

        Timestamp startTimestamp =
                (Timestamp) row.get(
                        "start_date_time"
                );

        Timestamp endTimestamp =
                (Timestamp) row.get(
                        "end_date_time"
                );

        LocalDateTime startDateTime =
                startTimestamp.toLocalDateTime();

        LocalDateTime endDateTime =
                endTimestamp.toLocalDateTime();

        RentalStatus status =
                RentalStatus.valueOf(
                        ((String) row.get("status"))
                                .toUpperCase()
                );

        return new Rental(
                rentalId,
                userId,
                cellId,
                startDateTime,
                endDateTime,
                status
        );
    }
}