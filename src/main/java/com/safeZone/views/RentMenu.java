package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;

import java.io.IOException;
import java.time.LocalDateTime;

import com.safeZone.util.JsonReader;
import com.safeZone.util.JsonData;
import com.safeZone.util.PaymentProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RentMenu {

    private static final Logger log = LoggerFactory.getLogger(RentMenu.class);
    private final PaymentProvider paymentProvider;

    private WindowBasedTextGUI gui;
    private AppMenus appMenus;

    public RentMenu(PaymentProvider paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public void setAppMenus(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void setGui(WindowBasedTextGUI gui) {
        this.gui = gui;
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
        BasicWindow dateWindow = new BasicWindow("Выбор срока аренды");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        TextBox rentHoursBox = new TextBox(new TerminalSize(10, 1));
        rentHoursBox.setText("1-24");

        JsonReader reader = new JsonReader();

        Button rent = new Button("Арендовать", () -> {
            int hours;
            try {
                hours = Integer.parseInt(rentHoursBox.getText().trim());
            } catch (NumberFormatException e) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Введите число часов от 1 до 24");
                return;
            }
            if (hours < 1 || hours > 24) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Срок аренды должен быть от 1 до 24 часов");
                return;
            }

            LocalDateTime rentUntil = LocalDateTime.now().plusHours(hours);

            try {
                JsonData data = reader.readDataFromJson("config.json");
                String token = data.getToken();
                try {
                    paymentProvider.createPayment(rentUntil, size, token);
                    dateWindow.close();
                    MessageDialog.showMessageDialog(gui, "Готово", "Аренда успешно оформлена");
                    appMenus.showMainMenu();
                } catch (Exception e) {
                    log.error("Ошибка при создании платежа: {}", e.getMessage());
                    MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось создать платёж");
                }
            } catch (IOException e) {
                log.error("Ошибка при чтении JSON: {}", e.getMessage());
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось прочитать конфигурацию");
            }
        });

        // TODO: ДОБАВИТЬ МЕТОД СОЗДАНИЯ ПЛАТЕЖКИ

        Button exit = new Button("Выход", () -> {
            dateWindow.close();
            appMenus.showMainMenu();
        });

        panel.addComponent(new Label("СРОК АРЕНДЫ (часы, 1-24)"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(rentHoursBox);
        panel.addComponent(new EmptySpace());
        panel.addComponent(rent);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        dateWindow.setComponent(panel);
        gui.addWindow(dateWindow);
    }
}
