package com.safezone.client.model;

import java.time.LocalDateTime;

/** Client data independent of the application's service entities. */
public final class Models {
    private Models() { }

    public enum Role {
        ADMIN("Администратор"), CLIENT("Клиент");
        private final String label;
        Role(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum UserStatus {
        ACTIVE("Активен"), BLOCKED("Заблокирован"), DELETED("Удалён");
        private final String label;
        UserStatus(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum CellSize {
        SMALL("Малая"), MEDIUM("Средняя"), LARGE("Большая");
        private final String label;
        CellSize(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum CellStatus {
        AVAILABLE("Свободна"), RENTED("Занята"), UNAVAILABLE("Недоступна");
        private final String label;
        CellStatus(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public enum RentalStatus {
        ACTIVE("Действует"), EXPIRED("Завершена"), CANCELLED("Отменена");
        private final String label;
        RentalStatus(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    public record User(int id, String login, Role role, UserStatus status) {
        public boolean isAdmin() { return role == Role.ADMIN; }
    }

    public record Warehouse(int id, String name, String address) { }

    public record Cell(int id, int warehouseId, int row, int column, String number,
                       CellSize size, CellStatus status) { }

    public record Rental(int id, int userId, int cellId, LocalDateTime startDateTime,
                         LocalDateTime endDateTime, RentalStatus status) { }

    public record CellStatistic(Cell cell, int rentalCount) { }
}
