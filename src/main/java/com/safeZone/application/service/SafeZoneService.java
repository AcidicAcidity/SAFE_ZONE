package com.safezone.application.service;

import com.safezone.application.exception.AuthenticationException;
import com.safezone.application.exception.CellUnavailableException;
import com.safezone.application.exception.UserAlreadyExistsException;
import com.safezone.application.exception.UserNotFoundException;
import com.safezone.domain.enums.CellSize;
import com.safezone.domain.enums.CellStatus;
import com.safezone.domain.enums.RentalStatus;
import com.safezone.domain.enums.Role;
import com.safezone.domain.enums.UserStatus;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Rental;
import com.safezone.domain.model.User;
import com.safezone.domain.model.Warehouse;
import com.safezone.infrastructure.data.repository.CellRepository;
import com.safezone.infrastructure.data.repository.RentalRepository;
import com.safezone.infrastructure.data.repository.UserRepository;
import com.safezone.infrastructure.data.repository.WarehouseRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SafeZoneService {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final CellRepository cellRepository;
    private final RentalRepository rentalRepository;
    private final PasswordService passwordService;

    public SafeZoneService(
            UserRepository userRepository,
            WarehouseRepository warehouseRepository,
            CellRepository cellRepository,
            RentalRepository rentalRepository,
            PasswordService passwordService) {

        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.cellRepository = cellRepository;
        this.rentalRepository = rentalRepository;
        this.passwordService = passwordService;
    }

    // =========================================================
    // AUTHORIZATION
    // =========================================================

    public User login(String login, String password) {

        try {
            User user = userRepository.findByLogin(login);

            if (user == null) {
                throw new AuthenticationException(
                        "Неверный логин или пароль."
                );
            }

            if (user.getStatus() == UserStatus.BLOCKED) {
                throw new AuthenticationException(
                        "Ваш аккаунт заблокирован."
                );
            }

            if (user.getStatus() == UserStatus.DELETED) {
                throw new AuthenticationException(
                        "Ваш аккаунт удалён."
                );
            }

            if (!passwordService.matches(
                    password,
                    user.getPasswordHash())) {

                throw new AuthenticationException(
                        "Неверный логин или пароль."
                );
            }

            return user;

        } catch (SQLException e) {

            e.printStackTrace();

            throw new IllegalStateException(
                    "Ошибка при обращении к базе данных: "
                            + e.getMessage(),
                    e
            );
        }
    }

    public User register(String login, String password) {

        validateRegistration(login, password);

        try {
            if (userRepository.existsByLogin(login)) {
                throw new UserAlreadyExistsException(
                        "Пользователь с таким логином уже существует."
                );
            }

            String passwordHash =
                    passwordService.hash(password);

            int userId =
                    userRepository.create(
                            login,
                            passwordHash
                    );

            return userRepository.findById(userId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка регистрации.",
                    e
            );
        }
    }

    private void validateRegistration(
            String login,
            String password) {

        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Логин не может быть пустым."
            );
        }

        if (login.length() < 3) {
            throw new IllegalArgumentException(
                    "Логин должен содержать минимум 3 символа."
            );
        }

        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException(
                    "Пароль должен содержать минимум 4 символа."
            );
        }
    }

    // =========================================================
    // USERS
    // =========================================================

    public List<User> getUsers() {

        try {
            return userRepository.findAll();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить пользователей.",
                    e
            );
        }
    }

    public User findUserById(int userId) {

        try {
            return userRepository.findById(userId);

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка поиска пользователя.",
                    e
            );
        }
    }

    public void blockUser(int userId) {
        updateUserStatus(
                userId,
                UserStatus.BLOCKED
        );
    }

    public void unblockUser(int userId) {
        updateUserStatus(
                userId,
                UserStatus.ACTIVE
        );
    }

    public void deleteUser(int userId) {
        updateUserStatus(
                userId,
                UserStatus.DELETED
        );
    }

    private void updateUserStatus(
            int userId,
            UserStatus status) {

        try {
            User user =
                    userRepository.findById(userId);

            if (user == null) {
                throw new UserNotFoundException(
                        "Пользователь не найден."
                );
            }

            userRepository.updateStatus(
                    userId,
                    status.name()
            );

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Ошибка изменения пользователя.",
                    e
            );
        }
    }

    // =========================================================
    // WAREHOUSES
    // =========================================================

    public List<Warehouse> getWarehouses() {

        try {
            return warehouseRepository.findAll();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить склады.",
                    e
            );
        }
    }

    // =========================================================
    // CELLS
    // =========================================================

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

    public List<Cell> getAvailableCells() {

        try {
            return cellRepository.findAvailable();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить свободные ячейки.",
                    e
            );
        }
    }

    /**
     * Проверяет, свободна ли ячейка
     * в указанный интервал времени.
     */
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

    public List<Cell> getAvailableCells(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        try {
            return cellRepository.findAvailable();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось получить ячейки.",
                    e
            );
        }
    }

    // =========================================================
    // RENTALS
    // =========================================================

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

        Cell cell = findCellById(cellId);

        if (cell == null) {
            throw new IllegalArgumentException(
                    "Ячейка не найдена."
            );
        }

        if (!isCellAvailable(
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

        List<Cell> cells = getCells();

        for (Cell cell : cells) {

            if (cell.getSize() != size) {
                continue;
            }

            if (cell.getStatus() == CellStatus.UNAVAILABLE) {
                continue;
            }

            if (isCellAvailable(
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