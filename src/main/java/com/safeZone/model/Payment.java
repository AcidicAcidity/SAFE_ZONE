package com.safeZone.model;
import java.time.LocalDateTime;

import org.postgresql.util.LruCache.CreateAction;

public class Payment {

    private int Order_ID;
    private Bin Bin_ID;
    private Bin PriceAtHour;
    private StatusOrder status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime end_rent_date;
    private User User_ID;

    public enum StatusOrder {
        PENDING(1, "ОЖИДАЕТ ОПЛАТЫ"),
        PAID(2, "ОПЛАЧЕН"),
        CANCELLED(3, "ОТМЕНЕН");

        private final int code;
        private final String description;

        StatusOrder(int code, String description) {
            this.code = code;
            this.description = description;
        }

        // Методы для получения кода и расшифровки статуса заказа
        public int getCodeStatusOrder() { return code; }
        public String getDescriptionOrder() { return description; }

        // Метод для получения enum статуса по коду пользователя из БД
        public static StatusOrder getStatusOrder(int code) {
            for (StatusOrder s : values()) {
                if (s.getCodeStatusOrder() == code) { return s; }
            } throw new IllegalArgumentException("Неизвестный код статуса заказа:" + code);
        }
    }
    public Payment(int Order_ID, Bin Bin_ID, Bin PriceAtHour, StatusOrder status, LocalDateTime created_at, LocalDateTime updated_at, LocalDateTime end_rent_date, User User_ID) {
        this.Order_ID = Order_ID;
        this.Bin_ID = Bin_ID;
        this.PriceAtHour = PriceAtHour;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.end_rent_date = end_rent_date;
        this.User_ID = User_ID;
    }

    // геттеры и сеттеры дописать в случае надобности
}
