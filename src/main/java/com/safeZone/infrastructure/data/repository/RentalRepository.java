package com.safezone.infrastructure.data.repository;

import com.safezone.domain.enums.RentalStatus;
import com.safezone.domain.model.Rental;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository {

    Rental findById(int rentalId) throws SQLException;

    List<Rental> findAll() throws SQLException;

    List<Rental> findByUserId(int userId) throws SQLException;

    int create(
            int userId,
            int cellId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) throws SQLException;

    void updateStatus(
            int rentalId,
            RentalStatus status
    ) throws SQLException;

    int countByCellId(int cellId) throws SQLException;

    boolean hasActiveRental(
            int cellId
    ) throws SQLException;

    boolean hasOverlappingRental(
            int cellId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) throws SQLException;

    void markExpiredRentals(
            LocalDateTime currentDateTime
    ) throws SQLException;
}