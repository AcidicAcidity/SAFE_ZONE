package com.safeZone;

import java.io.IOException;

public class App {
    public static void main(){
        JsonReader reader = new JsonReader();
        try {
            JsonData data = reader.readDataFromJson("config.json");
            System.out.println("Host: " + data.getHost());
            System.out.println("Port: " + data.getPort());
            System.out.println("Database: " + data.getDataBase());
        } catch (IOException e) {
            System.err.println("Ошибка чтения JSON: " + e.getMessage());
        }
    }
}
