package com.safeZone.model;
import java.time.LocalDateTime;


public class Payment {

    private int orderId;
    private Bin binId;
    private Bin priceAtHour;
    private StatusOrder status;
    private Integer rentTime; //На сколько часов арендован
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endRentDate;
    private User userId;

    public enum StatusOrder {
        PENDING(1, "ОЖИДАЕТ ОПЛАТЫ"),
        PAID(2, "ОПЛАЧЕН"),
        CANCELLED(3, "ОТМЕНЕН"),
        REFUNDED(4, "Возвращен");

        private final int code;
        private final String description;

        StatusOrder(int code, String description) {
            this.code = code;
            this.description = description;
        }

        // Методы для получения кода и расшифровки статуса заказа
        public int getCodeStatusOrder() { return code; }
        public String getTextStatusOrder() { return description; }

        // Метод для получения enum статуса по коду пользователя из БД
        public static StatusOrder getStatusOrder(int code) {
            for (StatusOrder s : values()) {
                if (s.getCodeStatusOrder() == code) { return s; }
            } throw new IllegalArgumentException("Неизвестный код статуса заказа:" + code);
        }

    }

    public Payment(int orderId, Bin binId, Bin priceAtHour, StatusOrder status, Integer rentTime, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime endRentDate, User userId) {
        this.orderId = orderId;
        this.binId = binId;
        this.priceAtHour = priceAtHour;
        this.status = status;
        this.rentTime = rentTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.endRentDate = endRentDate;
        this.userId = userId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public Integer getBinId() {
        return binId.getBinId();
    }

    public Integer getPriceAtHour() {
        return binId.getPriceAtHour();
    }

    public String getStatus() {
        return status.getTextStatusOrder();
    }

    public Integer getRentTime() {
        return rentTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getEndRentDate() {
        return endRentDate;
    }

    public Integer getOwnerId() {
        return userId.getUserId();
    }

    public Integer getAmount() {
        if (rentTime == null) return 0;
        return getPriceAtHour() * rentTime;
    }

}
