package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.*;
import java.io.IOException;
import java.time.LocalDateTime;



import com.safeZone.util.JsonReader;
import com.safeZone.util.JsonData;
import com.safeZone.util.PaymentProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RentMenu {

    private static final Logger log = LoggerFactory.getLogger(RentMenu.class);
    private static WindowBasedTextGUI gui;
    private PaymentProvider paymentProvider;

    private AppMenus appMenus;

    public RentMenu(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void showRentMenu() {
        BasicWindow rentWindow = new BasicWindow("Аренда");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button small = new Button("Маленькая", () -> {
            rentWindow.close();
            rentDateWindow("Маленькая");
        });

        Button medium = new Button("Средняя", () -> {
            rentWindow.close();
            rentDateWindow("Средняя");
        });

        Button large = new Button("Большая", () -> {
            rentWindow.close();
            rentDateWindow("Большая");
        });

        Button exit = new Button("Выход", () -> {
            rentWindow.close();
            appMenus.showMainMenu();
        });

        panel.addComponent(new Label("АРЕНДА ЯЧЕЙКИ"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Выберите размер желаемой ячейки"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(small);
        panel.addComponent(medium);
        panel.addComponent(large);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        rentWindow.setComponent(panel);
        gui.addWindowAndWait(rentWindow);
    }

    private void rentDateWindow(String size) {
        BasicWindow dateWindow = new BasicWindow("Выбор даты");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));


        TextBox rentTime = new TextBox(new TerminalSize(30, 2));
        rentTime.setText("1-24");

        JsonReader reader = new JsonReader();

        Button rent = new Button("Арендовать", () -> {
            dateWindow.close();
            LocalDateTime rentTimeValue = LocalDateTime.parse(rentTime.getText());
            try {
                JsonData data = reader.readDataFromJson("config.json");
                String token = data.getToken();
                try{
                    paymentProvider.createPayment(rentTimeValue, size, token);
                } catch (Exception e) {
                    log.error("Ошибка при создании платежа: {}", e.getMessage());
                }
            } catch (IOException e) {
                log.error("Ошибка при чтении JSON: {}", e.getMessage());
            }
        });

        Button exit = new Button("Выход", () -> {
            dateWindow.close();
            appMenus.showMainMenu();
        });

        panel.addComponent(new Label("СРОК АРЕНДЫ"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(rentTime);
        panel.addComponent(new EmptySpace());
        panel.addComponent(rent);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        dateWindow.setComponent(panel);
        gui.addWindowAndWait(dateWindow);
    }
}
