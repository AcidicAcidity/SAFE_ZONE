package com.safeZone.model;
public class User {
    private int ID;
    private String login;
    private String password;
    private Status status;
    private Role role;
    // Статус пользователя
    public enum Status {
        ACTIVE(1, "АКТИВЕН"),
        BLOCKED(2, "ЗАБЛОКИРОВАН"),
        DELITED(3, "УДАЛЕН");

        private final int code;
        private final String description;

        Status(int code, String description) {
            this.code = code;
            this.description = description;
        }
        // Методы для получения кода и расшифровки статуса
        public int getCodeStatus() { return code; }
        public String getDescriptionStatus() { return description; }
        // Метод для получения enum статуса по коду пользователя из БД
        public static Status getStatus(int code) {
            for (Status s : values()) {
                if (s.getCodeStatus() == code) { return s; }
            }
            throw new IllegalArgumentException("Неизвестный код статуса пользователя:" + code);
        }
    }
    public enum Role {
        CLIENT(1, "ПОЛЬЗОВАТЕЛЬ"),
        ADMIN(2, "АДМИНИСТРАТОР");

        private final int code;
        private final String description;
        Role(int code, String description) {
            this.code = code;
            this.description = description;
        }
        // Методы для получения кода и роли пользователя
        public int getCodeRole() { return code; }
        public String getDescriptionStatus() { return description; }
        // Метод для получения enum роли по коду пользователя из БД
        public static Role getRole(int code) {
            for (Role s : values()) {
                if (s.getCodeRole() == code) { return s; }
            }
            throw new IllegalArgumentException("Неизвестный код роли пользователя:" + code);
        }

    }
    public User(int ID, String login, String password, Status status, Role role) {
        this.ID = ID;
        this.login = login;
        this.password = password;
        this.status = status;
        this.role = role;
    }
}