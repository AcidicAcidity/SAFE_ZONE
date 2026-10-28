package com.safezone.client.net;

import com.safezone.application.exception.AuthenticationException;
import com.safezone.application.exception.CellUnavailableException;
import com.safezone.application.exception.UserAlreadyExistsException;
import com.safezone.application.exception.UserNotFoundException;
import com.safezone.application.service.AuthService;
import com.safezone.application.service.CellService;
import com.safezone.application.service.PasswordService;
import com.safezone.application.service.RentalService;
import com.safezone.application.service.UserService;
import com.safezone.application.service.WarehouseService;
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

/** Synchronous client boundary backed by the shared application services. */
public final class SafeZoneClient {
    private static final Set<String> VALIDATION_MESSAGES = Set.of(
            "Логин не может быть пустым.", "Логин должен содержать минимум 3 символа.",
            "Пароль должен содержать минимум 4 символа.", "Ячейка не найдена.",
            "Продолжительность аренды должна быть от 1 до 24 часов.", "Аренда не найдена.",
            "Эту аренду нельзя отменить.");
    private Services services;
    private volatile Models.User currentUser;

    public Models.User currentUser() { return currentUser; }

    public Models.User login(String login, String password) {
        Models.User user = call(() -> user(services().auth().login(login, password)),
                "Не удалось войти. Проверьте соединение и повторите попытку.");
        currentUser = user;
        return user;
    }

    public Models.User register(String login, String password) {
        return call(() -> user(services().auth().register(login, password)),
                "Не удалось зарегистрироваться. Повторите попытку.");
    }

    public void logout() { currentUser = null; }

    public List<Models.Warehouse> warehouses() {
        requireUser();
        return call(() -> services().warehouses().getWarehouses().stream().map(SafeZoneClient::warehouse).toList(),
                "Не удалось получить склады. Повторите попытку.");
    }

    public List<Models.Cell> cells() {
        requireUser();
        return call(() -> services().cells().getCells().stream().map(SafeZoneClient::cell).toList(),
                "Не удалось получить ячейки. Повторите попытку.");
    }

    public List<Models.Cell> availableCells() {
        requireUser();
        return call(() -> services().cells().getAvailableCells().stream().map(SafeZoneClient::cell).toList(),
                "Не удалось получить свободные ячейки. Повторите попытку.");
    }

    public List<Models.Rental> myRentals() {
        Models.User user = requireUser();
        return call(() -> services().rentals().getUserRentals(user.id()).stream().map(SafeZoneClient::rental).toList(),
                "Не удалось получить ваши аренды. Повторите попытку.");
    }

    public List<Models.Rental> rentals() {
        requireAdmin();
        return call(() -> services().rentals().getRentals().stream().map(SafeZoneClient::rental).toList(),
                "Не удалось получить бронирования. Повторите попытку.");
    }

    public List<Models.User> users() {
        requireAdmin();
        return call(() -> services().users().getUsers().stream().map(SafeZoneClient::user).toList(),
                "Не удалось получить пользователей. Повторите попытку.");
    }

    public List<Models.CellStatistic> cellStatistics() {
        requireAdmin();
        return call(() -> services().cells().getCells().stream()
                        .map(value -> new Models.CellStatistic(cell(value), services().rentals().getCellRentalCount(value.getCellId())))
                        .toList(),
                "Не удалось получить статистику по ячейкам. Повторите попытку.");
    }

    public Models.Rental rentSpecificCell(int cellId, int hours) {
        Models.User user = requireUser();
        return call(() -> rental(services().rentals().rentSpecificCell(user.id(), cellId, LocalDateTime.now(), hours)),
                "Не удалось оформить аренду. Повторите попытку.");
    }

    public Models.Rental rentCellBySize(Models.CellSize size, int hours) {
        Models.User user = requireUser();
        return call(() -> rental(services().rentals().rentCellBySize(user.id(),
                        com.safezone.domain.enums.CellSize.valueOf(size.name()), LocalDateTime.now(), hours)),
                "Не удалось оформить аренду. Повторите попытку.");
    }

    public void blockUser(int userId) {
        requireAdmin();
        call(() -> { services().users().blockUser(userId); return null; }, "Не удалось заблокировать пользователя.");
    }

    public void unblockUser(int userId) {
        requireAdmin();
        call(() -> { services().users().unblockUser(userId); return null; }, "Не удалось разблокировать пользователя.");
    }

    public void deleteUser(int userId) {
        requireAdmin();
        call(() -> { services().users().deleteUser(userId); return null; }, "Не удалось удалить аккаунт.");
    }

    public void cancelRental(int rentalId) {
        requireAdmin();
        call(() -> { services().rentals().cancelRental(rentalId); return null; }, "Не удалось отменить бронь.");
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

    private synchronized Services services() {
        if (services == null) {
            DBHelper database = new DBHelper();
            new DatabaseInitializer(database).initialize();
            var userRepository = new PostgresUserRepository(database);
            var warehouseRepository = new PostgresWarehouseRepository(database);
            var cellRepository = new PostgresCellRepository(database);
            var rentalRepository = new PostgresRentalRepository(database);
            var cellService = new CellService(cellRepository, rentalRepository);
            services = new Services(new AuthService(userRepository, new PasswordService()), cellService,
                    new RentalService(rentalRepository, cellService), new UserService(userRepository),
                    new WarehouseService(warehouseRepository));
        }
        return services;
    }

    private record Services(AuthService auth, CellService cells, RentalService rentals,
                            UserService users, WarehouseService warehouses) { }

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
