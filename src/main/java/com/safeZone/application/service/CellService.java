package com.safezone.application.service;


import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.model.Cell;
import com.safezone.infrastructure.data.repository.CellRepository;
import com.safezone.infrastructure.data.repository.RentalRepository;



import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class CellService {

    private final CellRepository cellRepository;
    private final RentalRepository rentalRepository;

    public CellService(CellRepository cellRepository, RentalRepository rentalRepository) {
        this.cellRepository = cellRepository;
        this.rentalRepository = rentalRepository;
    }


    public List<Cell> getCells() {

        try {
            return cellRepository.findAll();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить ячейки.",
                    e
            );
        }
    }

    public Cell findCellById(int cellId) {

        try {
            return cellRepository.findById(cellId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка поиска ячейки.",
                    e
            );
        }
    }

    public List<Cell> getAvailableCells(LocalDateTime start, LocalDateTime end) {
        List<Cell> result = new ArrayList<>();
        for (Cell cell: getCells()) {
            if (isCellAvailable(cell.getCellId(), start, end)) {
               result.add(cell);
            }
        }

        return result;
    }

    public List<Cell> getAvailableCells() {
       try {
           List<Cell> cells = cellRepository.findAvailable();
           List<Cell> availableCells = new ArrayList<>();
           for (Cell cell : cells) {
               if (!rentalRepository.hasActiveRental(cell.getCellId())) {
                   availableCells.add(cell);
               }
           }
           return availableCells;
       } catch (SQLException e) {
           throw new IllegalStateException("Не удалось получить доступные ячейки", e);
       }
    }

    public List<Cell> findByWarehouseId(int warehouseId) {
        try {
            return cellRepository.findByWarehouseId(warehouseId);
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось получить список ячеек конкретного склада,");
        }
    }


    public int createCell(int warehouseId, int row, int column, String size) {
        try {
            cellRepository.createCell(warehouseId, row, column, size);
            return -1;
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось создать новую ячейку");
        }
    }

    //Проверяет, свободна ли ячейка в указанный интервал времени.

    public boolean isCellAvailable(
            int cellId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        Cell cell = findCellById(cellId);

        if (cell == null) {
            throw new IllegalArgumentException(
                    "Ячейка не найдена."
            );
        }

        if (cell.getStatus() == CellStatus.UNAVAILABLE) {
            return false;
        }

        try {
            return !rentalRepository.hasOverlappingRental(
                    cellId,
                    startDateTime,
                    endDateTime
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось проверить доступность ячейки.",
                    e
            );
        }
    }
}
