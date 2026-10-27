package com.safezone.client.net;

import com.safezone.application.exception.AuthenticationException;
import com.safezone.application.exception.CellUnavailableException;
import com.safezone.application.exception.UserAlreadyExistsException;
import com.safezone.application.exception.UserNotFoundException;
import com.safezone.application.service.PasswordService;
import com.safezone.application.service.SafeZoneService;
import com.safezone.client.model.Models;
import com.safezone.domain.model.Cell;
import com.safezone.domain.model.Rental;
import com.safezone.domain.model.User;
import com.safezone.domain.model.Warehouse;
import com.safezone.infrastructure.data.DBHelper;
import com.safezone.infrastructure.data.DatabaseInitializer;
import com.safezone.infrastructure.data.repository.PostgresCellRepository;
import com.safezone.infrastructure.data.repository.PostgresRentalRepository;
import com.safezone.infrastructure.data.repository.PostgresUserRepository;
import com.safezone.infrastructure.data.repository.PostgresWarehouseRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Synchronous client boundary backed temporarily by the existing console service. */
public final class SafeZoneClient {
    private static final Set<String> VALIDATION_MESSAGES = Set.of(
            "Логин не может быть пустым.", "Логин должен содержать минимум 3 символа.",
            "Пароль должен содержать минимум 4 символа.", "Ячейка не найдена.",
            "Продолжительность аренды должна быть от 1 до 24 часов.", "Аренда не найдена.",
            "Эту аренду нельзя отменить.");
    private SafeZoneService service;
    private volatile Models.User currentUser;

    public Models.User currentUser() { return currentUser; }

    public Models.User login(String login, String password) {
        Models.User user = call(() -> user(service().login(login, password)),
                "Не удалось войти. Проверьте соединение и повторите попытку.");
        currentUser = user;
        return user;
    }

    public Models.User register(String login, String password) {
        return call(() -> user(service().register(login, password)),
                "Не удалось зарегистрироваться. Повторите попытку.");
    }

    public void logout() { currentUser = null; }

    public List<Models.Warehouse> warehouses() {
        requireUser();
        return call(() -> service().getWarehouses().stream().map(SafeZoneClient::warehouse).toList(),
                "Не удалось получить склады. Повторите попытку.");
    }

    public List<Models.Cell> cells() {
        requireUser();
        return call(() -> service().getCells().stream().map(SafeZoneClient::cell).toList(),
                "Не удалось получить ячейки. Повторите попытку.");
    }

    public List<Models.Cell> availableCells() {
        requireUser();
        return call(() -> service().getAvailableCells().stream().map(SafeZoneClient::cell).toList(),
                "Не удалось получить свободные ячейки. Повторите попытку.");
    }

    public List<Models.Rental> myRentals() {
        Models.User user = requireUser();
        return call(() -> service().getUserRentals(user.id()).stream().map(SafeZoneClient::rental).toList(),
                "Не удалось получить ваши аренды. Повторите попытку.");
    }

    public List<Models.Rental> rentals() {
        requireAdmin();
        return call(() -> service().getRentals().stream().map(SafeZoneClient::rental).toList(),
                "Не удалось получить бронирования. Повторите попытку.");
    }

    public List<Models.User> users() {
        requireAdmin();
        return call(() -> service().getUsers().stream().map(SafeZoneClient::user).toList(),
                "Не удалось получить пользователей. Повторите попытку.");
    }

    public List<Models.CellStatistic> cellStatistics() {
        requireAdmin();
        return call(() -> service().getCells().stream()
                        .map(value -> new Models.CellStatistic(cell(value), service().getCellRentalCount(value.getCellId())))
                        .toList(),
                "Не удалось получить статистику по ячейкам. Повторите попытку.");
    }

    public Models.Rental rentSpecificCell(int cellId, int hours) {
        Models.User user = requireUser();
        return call(() -> rental(service().rentSpecificCell(user.id(), cellId, LocalDateTime.now(), hours)),
                "Не удалось оформить аренду. Повторите попытку.");
    }

    public Models.Rental rentCellBySize(Models.CellSize size, int hours) {
        Models.User user = requireUser();
        return call(() -> rental(service().rentCellBySize(user.id(),
                        com.safezone.domain.enums.CellSize.valueOf(size.name()), LocalDateTime.now(), hours)),
                "Не удалось оформить аренду. Повторите попытку.");
    }

    public void blockUser(int userId) {
        requireAdmin();
        call(() -> { service().blockUser(userId); return null; }, "Не удалось заблокировать пользователя.");
    }

    public void unblockUser(int userId) {
        requireAdmin();
        call(() -> { service().unblockUser(userId); return null; }, "Не удалось разблокировать пользователя.");
    }

    public void deleteUser(int userId) {
        requireAdmin();
        call(() -> { service().deleteUser(userId); return null; }, "Не удалось удалить аккаунт.");
    }

    public void cancelRental(int rentalId) {
        requireAdmin();
        call(() -> { service().cancelRental(rentalId); return null; }, "Не удалось отменить бронь.");
    }

    public Path exportDatabase(Path destination) {
        requireAdmin();
        try {
            return LocalExport.exportTo(destination);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new ClientException("Экспорт прерван.");
        } catch (Exception failure) {
            throw new ClientException("Не удалось экспортировать базу данных. Проверьте выбранный файл и повторите попытку.");
        }
    }

    private Models.User requireUser() {
        Models.User user = currentUser;
        if (user == null) throw new ClientException("Войдите в аккаунт.");
        return user;
    }

    private void requireAdmin() {
        if (!requireUser().isAdmin()) throw new ClientException("Недостаточно прав для этого действия.");
    }

    private synchronized SafeZoneService service() {
        if (service == null) {
            DBHelper database = new DBHelper();
            new DatabaseInitializer(database).initialize();
            service = new SafeZoneService(new PostgresUserRepository(database), new PostgresWarehouseRepository(database),
                    new PostgresCellRepository(database), new PostgresRentalRepository(database), new PasswordService());
        }
        return service;
    }

    private static <T> T call(Supplier<T> work, String fallback) {
        try {
            return work.get();
        } catch (ClientException error) {
            throw error;
        } catch (RuntimeException error) {
            boolean businessError = error instanceof AuthenticationException || error instanceof CellUnavailableException
                    || error instanceof UserAlreadyExistsException || error instanceof UserNotFoundException;
            boolean validationError = error instanceof IllegalArgumentException || error instanceof IllegalStateException;
            String message = error.getMessage();
            if (error.getCause() == null && message != null
                    && (businessError || validationError && VALIDATION_MESSAGES.contains(message))) {
                throw new ClientException(message);
            }
            throw new ClientException(fallback);
        }
    }

    private static Models.User user(User value) {
        return new Models.User(value.getUserId(), value.getLogin(), Models.Role.valueOf(value.getRole().name()),
                Models.UserStatus.valueOf(value.getStatus().name()));
    }

    private static Models.Warehouse warehouse(Warehouse value) {
        return new Models.Warehouse(value.getWarehouseId(), value.getName(), value.getAddress());
    }

    private static Models.Cell cell(Cell value) {
        return new Models.Cell(value.getCellId(), value.getWarehouseId(), value.getRow(), value.getColumn(),
                value.getNumber(), Models.CellSize.valueOf(value.getSize().name()), Models.CellStatus.valueOf(value.getStatus().name()));
    }

    private static Models.Rental rental(Rental value) {
        return new Models.Rental(value.getRentalId(), value.getUserId(), value.getCellId(), value.getStartDateTime(),
                value.getEndDateTime(), Models.RentalStatus.valueOf(value.getStatus().name()));
    }
}
