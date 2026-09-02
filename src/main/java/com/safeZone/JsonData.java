package com.safeZone;
// Файл описывающий структуру JSON
//
// Поля класса обязательно идентичны полям в JSON
public class JsonData {
    private String host;
    private int port;
    private String database;

    // Пустой конструктор для Jackson
    public JsonData(){
    }

    // Блок геттеров и сеттеров для Jackson
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDataBase() {
        return database;
    }

    public void setDataBase(String database) {
        this.database = database;
    }
}
