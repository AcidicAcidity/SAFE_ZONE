package com.safezone.application.service;

import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.enums.RentalStatus;
import com.safezone.domain.enums.Role;
import com.safezone.domain.enums.UserStatus;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Rental;
import com.safezone.domain.model.User;
import com.safezone.domain.model.Warehouse;
import com.safezone.infrastructure.data.TestDataInitializer;
import com.safezone.domain.enums.CellSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SafeZoneService {

    private final List<User> users = new ArrayList<>();
    private final List<Warehouse> warehouses = new ArrayList<>();
    private final List<Cell> cells = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();

    public SafeZoneService() {
        initializeTestData();
    }

    private void initializeTestData() {
        TestDataInitializer initializer =
                new TestDataInitializer();

        initializer.initializeWarehouses(warehouses);

        for (Warehouse warehouse : warehouses) {
            initializer.initializeCells(
                    cells,
                    warehouse.getWarehouseId()
            );
        }

        initializeUsers();
    }

    private void initializeUsers() {
        users.add(new User(
                1,
                "admin",
                "admin",
                UserStatus.ACTIVE,
                Role.ADMIN
        ));

        users.add(new User(
                2,
                "client",
                "client",
                UserStatus.ACTIVE,
                Role.CLIENT
        ));
    }

    public List<Warehouse> getWarehouses() {
        return warehouses;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Cell> getAvailableCells() {
        return cells.stream()
                .filter(cell ->
                        cell.getStatus() == CellStatus.AVAILABLE
                )
                .collect(Collectors.toList());
    }

    public User findUserByLogin(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }

        return null;
    }

    public User findUserById(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }

        return null;
    }

    public void blockUser(int userId) {

        User user = findUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        user.setStatus(UserStatus.BLOCKED);
    }

    public void unblockUser(int userId) {

        User user = findUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Пользователь не найден."
            );
        }

        user.setStatus(UserStatus.ACTIVE);
    }

    public void deleteUser(int userId) {

        User user = findUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException(
                    "Пользователь не найден."
            );
        }

        user.setStatus(UserStatus.DELETED);
    }

    public int getCellRentalCount(int cellId) {

        int count = 0;

        for (Rental rental : rentals) {

            if (rental.getCellId() == cellId) {
                count++;
            }
        }

        return count;
    }

    public Rental findRentalById(int rentalId) {

        for (Rental rental : rentals) {

            if (rental.getRentalId() == rentalId) {
                return rental;
            }
        }

        return null;
    }

    public void cancelRental(int rentalId) {

        Rental rental = findRentalById(rentalId);

        if (rental == null) {
            throw new IllegalArgumentException(
                    "Бронь не найдена."
            );
        }

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Эту бронь нельзя отменить."
            );
        }

        rental.setStatus(RentalStatus.CANCELLED);

        Cell cell = findCellById(
                rental.getCellId()
        );

        if (cell != null) {
            cell.setStatus(CellStatus.AVAILABLE);
        }
    }

    public Cell findCellById(int cellId) {
        for (Cell cell : cells) {
            if (cell.getCellId() == cellId) {
                return cell;
            }
        }

        return null;
    }

    public List<Rental> getUserRentals(int userId) {
        return rentals.stream()
                .filter(rental ->
                        rental.getUserId() == userId
                )
                .collect(Collectors.toList());
    }

    public Rental rentSpecificCell(
            int userId,
            int cellId,
            LocalDateTime startDateTime,
            int hours) {

        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException(
                    "Продолжительность аренды должна быть от 1 до 24 часов."
            );
        }

        Cell cell = findCellById(cellId);

        if (cell == null) {
            throw new IllegalArgumentException(
                    "Ячейка не найдена."
            );
        }

        if (cell.getStatus() != CellStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Эта ячейка недоступна для аренды."
            );
        }

        LocalDateTime endDateTime = startDateTime.plusHours(hours);

        int rentalId = rentals.size() + 1;

        Rental rental = new Rental(
                rentalId,
                userId,
                cellId,
                startDateTime,
                endDateTime,
                RentalStatus.ACTIVE
        );

        rentals.add(rental);
        cell.setStatus(CellStatus.RENTED);

        return rental;
    }

    public Rental rentCellBySize(
            int userId,
            CellSize size,
            LocalDateTime startDateTime,
            int hours) {

        if (hours < 1 || hours > 24) {
            throw new IllegalArgumentException(
                    "Продолжительность аренды должна быть от 1 до 24 часов."
            );
        }

        Cell selectedCell = null;

        for (Cell cell : cells) {
            if (cell.getSize() == size
                    && cell.getStatus() == CellStatus.AVAILABLE) {

                selectedCell = cell;
                break;
            }
        }

        if (selectedCell == null) {
            throw new IllegalStateException(
                    "Свободной ячейки выбранного размера нет."
            );
        }

        LocalDateTime endDateTime =
                startDateTime.plusHours(hours);

        int rentalId = rentals.size() + 1;

        Rental rental = new Rental(
                rentalId,
                userId,
                selectedCell.getCellId(),
                startDateTime,
                endDateTime,
                RentalStatus.ACTIVE
        );

        rentals.add(rental);
        selectedCell.setStatus(CellStatus.RENTED);

        return rental;
    }
}