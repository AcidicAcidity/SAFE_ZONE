package com.safeZone;

import org.slf4j.*;
import java.io.IOException;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

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
            System.out.println("User: " + data.getUser());
            System.out.println("Database: " + data.getDataBase());
            System.out.println("Password: " + data.getPassword());
        } catch (IOException e) {
            System.err.println("Ошибка чтения JSON: " + e.getMessage());
        }

        try {
            boolean isActive = DBHelper.CheckDBConnection();
            if (isActive == true) {
                log.info("DATABASE SUCCSESSFULLY CONNECTED");
            } else {
                log.error("DATABASE CONNECTION FAILED");
            }
        } catch (Exception e) {
            log.error("ERROR READ CONFIG FILE");
        }

        // // Создание экрана для вывода интерфейса.
        // Screen screen = new DefaultTerminalFactory().createScreen();
        // screen.startScreen();

        // Panel menuPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        // menuPanel.addComponent(new Button("Аренда ячейки", () -> {}));
        // menuPanel.addComponent(new Button("Войти как администратор", () -> {}));
        // menuPanel.addComponent(new Button("Выход", () -> {}));

        // Table<String> binsTable = new Table<>();
    }
}
