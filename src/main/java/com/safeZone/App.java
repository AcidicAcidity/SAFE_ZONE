package com.safeZone;

import org.slf4j.*;
import java.io.IOException;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

public class App {
    public static void main(){

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            .replace(":", "-")
            .replace("T", "_")
            .replace("Z", "");

        String logFileName = "logs/app_" + timestamp + ".log";

        File logDir = new File("logs");
        if (!logDir.exists()){
            logDir.mkdirs();
        }

        System.setProperty("log.file.name", logFileName);

        Logger log = LoggerFactory.getLogger(App.class);
        log.info("Приложение запущено. Лог-файл: {}", logFileName);

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
