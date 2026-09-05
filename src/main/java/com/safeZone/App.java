package com.safeZone;

import org.slf4j.*;
import java.io.IOException;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
// import com.googlecode.lanterna.gui2.*;
// import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
// import com.googlecode.lanterna.screen.Screen;
// import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.safeZone.util.*;


public class App {
    public static void main(){
        DBHelper dbhelp = new DBHelper();


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
            System.out.println("User: " + data.getUser());
            System.out.println("Database: " + data.getDataBase());
            System.out.println("Password: " + data.getPassword());
        } catch (IOException e) {
            System.err.println("Ошибка чтения JSON: " + e.getMessage());
        }

        try {
            boolean isActive = dbhelp.CheckDBConnection();
            if (isActive == true) {
                log.info("DATABASE SUCCSESSFULLY CONNECTED");
            } else {
                log.info("DATABASE CONNECTION FAILED");
            }
        } catch (Exception e) {
            log.info("ERROR READ CONFIG FILE");
        }


    }
}
