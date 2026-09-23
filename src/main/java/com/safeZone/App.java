package com.safeZone;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.safeZone.util.*;

import com.safeZone.views.*;


public class App {
    public static void main(String[] args){

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
            System.out.println("Database: " + data.getDatabase());
            System.out.println("Password: " + data.getPassword());
        } catch (IOException e) {
            System.err.println("Ошибка чтения JSON: " + e.getMessage());
        }

        DBUtils dbUtils;
        try {
            dbUtils = new DBUtils();
        } catch (IOException e) {
            log.error("ERROR INIT CONNECT DATABASE: {}", e.getMessage(), e);
            return;
        }

        DBHelper dbHelper = new DBHelper(dbUtils);

        if (dbHelper.checkDBConnection()) {
            log.info("SUCCES CONNECTION DB");
        } else {
            log.error("FAILED CONNECTION DB");
        }

        SafeZoneService service = new SafeZoneService(dbHelper);
        PaymentProvider paymentProvider = new PaymentProvider(dbHelper, service);
        RentMenu rentMenu = new RentMenu(paymentProvider);
        BinMenu binMenu = new BinMenu(service);
        PaymentMenu paymentMenu = new PaymentMenu(service);
        UserMenu userMenu = new UserMenu(service);
        StatsMenu statsMenu = new StatsMenu(service);
        createExport export = new createExport();

        AppMenus appMenus = new AppMenus(dbUtils, export, rentMenu, binMenu, paymentMenu, userMenu, statsMenu);

        try {
            new AuthMenu(dbHelper, service, appMenus).start();
        } catch (Exception e) {
            log.error("GUI START ERROR: ", e);
        }
    }
}
