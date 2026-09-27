package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;

import com.safeZone.util.createExport;
import com.safeZone.util.DBUtils;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AppMenus {

    private static final Logger log = LoggerFactory.getLogger(AppMenus.class);

    private final DBUtils dbUtils;
    private final createExport export;

    private WindowBasedTextGUI gui;

    private final RentMenu rentMenu;
    private final BinMenu binMenu;
    private final PaymentMenu paymentMenu;
    private final UserMenu userMenu;
    private final StatsMenu statsMenu;

    public AppMenus(DBUtils dbUtils, createExport export,
                     RentMenu rentMenu, BinMenu binMenu, PaymentMenu paymentMenu,
                     UserMenu userMenu, StatsMenu statsMenu) {
        this.dbUtils = dbUtils;
        this.export = export;
        this.rentMenu = rentMenu;
        this.binMenu = binMenu;
        this.paymentMenu = paymentMenu;
        this.userMenu = userMenu;
        this.statsMenu = statsMenu;

        rentMenu.setAppMenus(this);
        binMenu.setAppMenus(this);
        paymentMenu.setAppMenus(this);
        userMenu.setAppMenus(this);
        statsMenu.setAppMenus(this);
    }

    public void start(WindowBasedTextGUI gui) {
        this.gui = gui;
        rentMenu.setGui(gui);
        binMenu.setGui(gui);
        paymentMenu.setGui(gui);
        userMenu.setGui(gui);
        statsMenu.setGui(gui);
        showMainMenu();
    }

    public WindowBasedTextGUI getGui() {
        return gui;
    }

    public static boolean containsDigit(String str) {
        if (str == null) return false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void showMainMenu() {
        BasicWindow mainWindow = new BasicWindow("Главное меню");
        mainWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIXED_SIZE));
        mainWindow.setFixedSize(new TerminalSize(50, 20));
        Panel panel = new Panel(new GridLayout(1)
            .setHorizontalSpacing(2)
            .setVerticalSpacing(1)
            .setLeftMarginSize(4)
            .setRightMarginSize(1)
            .setTopMarginSize(1)
            .setBottomMarginSize(1));

        Label title = new Label("S A F E   Z O N E");
        title.setLayoutData(GridLayout.createLayoutData(
            GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER,
            true, false));

        Label subtitle = new Label("Система управления ячейками");
        subtitle.setLayoutData(GridLayout.createLayoutData(
            GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER));

        panel.addComponent(title);
        panel.addComponent(subtitle);
        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        panel.addComponent(makeFullWidthButton("Зарегестрировать аренду", () -> {
            mainWindow.close();
            rentMenu.showRentMenu();
        }));
        panel.addComponent(makeFullWidthButton("Найти сущность (ячейка/платеж)", () -> {
            mainWindow.close();
            showFilterWindow();
        }));
        panel.addComponent(makeFullWidthButton("Список пользователей", () -> {
            mainWindow.close();
            userMenu.findUser();
        }));
        panel.addComponent(makeFullWidthButton("Статистика ячеек", () -> {
            mainWindow.close();
            statsMenu.showStats();
        }));
        panel.addComponent(makeFullWidthButton("Экспорт данных", () -> {
            try {
                String pathToCSV = System.getProperty("user.home") + File.separator + "Downloads";
                createExport.exportDatabase(dbUtils.getConnection(), pathToCSV);
                MessageDialog.showMessageDialog(gui, "Экспорт", "Данные успешно экспортированы\n, ");
            } catch (Exception e) {
                log.error("Ошибка экспорта", e);
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось экспортировать данные");
            }
        }));
        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        panel.addComponent(makeFullWidthButton("Выход", () -> {
            mainWindow.close();
            System.exit(0);
        }));

        mainWindow.setComponent(panel);
        gui.addWindow(mainWindow);
    }

    private void showFilterWindow() {
        BasicWindow filterWindow = new BasicWindow("Поиск ячейки или платежа");
        filterWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIXED_SIZE));
        filterWindow.setFixedSize(new TerminalSize(50, 20));
        Panel panel = new Panel(new GridLayout(1)
            .setHorizontalSpacing(2)
            .setVerticalSpacing(1)
            .setLeftMarginSize(4)
            .setRightMarginSize(4)
            .setTopMarginSize(1)
            .setBottomMarginSize(1));

        Label title = new Label("ВЫБЕРИТЕ СУЩНОСТЬ ДЛЯ ПОИСКА");
        title.setLayoutData(GridLayout.createLayoutData(
            GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER,
            true, false));
        panel.addComponent(title);
        panel.addComponent(makeFullWidthButton("Платежи", () -> {
            filterWindow.close();
            paymentMenu.paymentWindow();
        }));
        panel.addComponent(makeFullWidthButton("Ячейки", () -> {
            filterWindow.close();
            binMenu.binWindow();
        }));
        panel.addComponent(new EmptySpace());
        panel.addComponent(makeFullWidthButton("Выход", () -> {
            filterWindow.close();
            showMainMenu();
        }));

        filterWindow.setComponent(panel);
        gui.addWindow(filterWindow);
    }

    private Button makeFullWidthButton(String text, Runnable action) {
        Button b = new Button(text, action);
        b.setLayoutData(GridLayout.createLayoutData(
            GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER,
            true, false));
        return b;
    }
}
