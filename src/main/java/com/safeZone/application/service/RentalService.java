package com.safezone.application.service;

import com.safezone.application.exception.CellUnavailableException;
import com.safezone.domain.enums.CellSize;
import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.enums.RentalStatus;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Rental;
import com.safezone.infrastructure.data.repository.RentalRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RentalService {

    private final RentalRepository rentalRepository;
    private final CellService cellService;

    public RentalService(RentalRepository rentalRepository, CellService cellService) {
        this.rentalRepository = rentalRepository;
        this.cellService = cellService;
    }


    public List<Rental> getRentals() {

        try {
            return rentalRepository.findAll();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить аренды.",
                    e
            );
        }
    }

    public List<Rental> getUserRentals(int userId) {

        try {
            return rentalRepository.findByUserId(userId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить аренды пользователя.",
                    e
            );
        }
    }

    public Rental findRentalById(int rentalId) {

        try {
            return rentalRepository.findById(rentalId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка поиска аренды.",
                    e
            );
        }
    }

    public Rental rentSpecificCell(
            int userId,
            int cellId,
            LocalDateTime startDateTime,
            int hours) {

        validateRentalHours(hours);

        LocalDateTime endDateTime =
                startDateTime.plusHours(hours);

        Cell cell = cellService.findCellById(cellId);

        if (cell == null) {
            throw new IllegalArgumentException(
                    "Ячейка не найдена."
            );
        }

        if (!cellService.isCellAvailable(
                cellId,
                startDateTime,
                endDateTime)) {

            throw new CellUnavailableException(
                    "Эта ячейка занята в выбранный период."
            );
        }

        try {
            int rentalId =
                    rentalRepository.create(
                            userId,
                            cellId,
                            startDateTime,
                            endDateTime
                    );

            return rentalRepository.findById(rentalId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось создать аренду.",
                    e
            );
        }
    }

    public Rental rentCellBySize(
            int userId,
            CellSize size,
            LocalDateTime startDateTime,
            int hours) {

        validateRentalHours(hours);

        LocalDateTime endDateTime =
                startDateTime.plusHours(hours);

        List<Cell> cells = cellService.getCells();

        for (Cell cell : cells) {

            if (cell.getSize() != size) {
                continue;
            }

            if (cell.getStatus() == CellStatus.UNAVAILABLE) {
                continue;
            }

            if (cellService.isCellAvailable(
                    cell.getCellId(),
                    startDateTime,
                    endDateTime)) {

                try {
                    int rentalId =
                            rentalRepository.create(
                                    userId,
                                    cell.getCellId(),
                                    startDateTime,
                                    endDateTime
                            );

                    return rentalRepository.findById(
                            rentalId
                    );

                } catch (SQLException e) {
                    throw new IllegalStateException(
                            "Не удалось создать аренду.",
                            e
                    );
                }
            }
        }

        throw new CellUnavailableException(
                "Свободной ячейки выбранного размера " +
                        "на данный период нет."
        );
    }

    private void validateRentalHours(int hours) {

        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException(
                    "Продолжительность аренды должна " +
                            "быть от 1 до 24 часов."
            );
        }
    }

    public void cancelRental(int rentalId) {

        Rental rental =
                findRentalById(rentalId);

        if (rental == null) {
            throw new IllegalArgumentException(
                    "Аренда не найдена."
            );
        }

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Эту аренду нельзя отменить."
            );
        }

        try {
            rentalRepository.updateStatus(
                    rentalId,
                    RentalStatus.CANCELLED
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось отменить аренду.",
                    e
            );
        }
    }

    public int getCellRentalCount(int cellId) {

        try {
            return rentalRepository.countByCellId(cellId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить статистику ячейки.",
                    e
            );
        }
    }

    public void updateExpiredRentals() {

        try {
            rentalRepository.markExpiredRentals(
                    LocalDateTime.now()
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось обновить статусы аренд.",
                    e
            );
        }
    }
}
