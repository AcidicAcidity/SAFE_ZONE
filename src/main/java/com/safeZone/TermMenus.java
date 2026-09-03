package com.safeZone;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.awt.Label;
import java.util.*;


public class TermMenus {

    private static WindowBasedTextGUI gui;

    public static void main() throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        gui = new MultiWindowTextGUI(screen);

        showMainMenu();

        screen.stopScreen();
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

    private static void showMainMenu() {
        BasicWindow mainWindow = new BasicWindow("Главное меню");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button rent = new Button("Зарегестрировать аренду", () -> {
            mainWindow.close();
            showSizeWindow();
        });
        Button filter = new Button("Найти сущность (ячейка/платеж)", () -> {
            mainWindow.close();
            showFilterWindow();
        });
        Button userSearch = new Button("Список пользователей", () -> {
            mainWindow.close();
            findUserWindow();
        });
        Button stats = new Button("Статистика ячеек", () -> {
            mainWindow.close();
            showStatsWindow();
        });
        Button export = new Button("Экспорт данных", () -> {
            mainWindow.close();
            showExportWindow();
        });
        Button exit = new Button("Выход", () -> {
            mainWindow.close();
            System.exit(0);
        });

        panel.addComponent(rent);
        panel.addComponent(filter);
        panel.addComponent(stats);
        panel.addComponent(export);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        mainWindow.setComponent(panel);
        gui.addWindowAndWait(mainWindow);
    }

    private static void showSizeWindow() {
        BasicWindow sizeWindow = new BasicWindow("Аренда");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button small = new Button("Маленькая", () -> {
            sizeWindow.close();
            showDateWindow();
        });
        Button middle = new Button("Средняя", () -> {
            sizeWindow.close();
            showDateWindow();
        });
        Button big = new Button("Большая", () -> {
            sizeWindow.close();
            showDateWindow();
        });
        Button exit = new Button("Выход", () -> {
            sizeWindow.close();
            showMainMenu();
        });

        panel.addComponent(new Label("ВЫБЕРИТЕ РАЗМЕР ЯЧЕЙКИ"));
        panel.addComponent(small);
        panel.addComponent(middle);
        panel.addComponent(big);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        sizeWindow.setComponent(panel);
        gui.addWindowAndWait(sizeWindow);
    }

    private static void showFilterWindow() {
        BasicWindow filterWindow = new BasicWindow("Поиск ячейки или платежа");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Label DisplayLabel = new Label("ВЫБЕРИТЕ СУЩНОСТЬ ДЛЯ ПОИСКА: ");

        Button bin = new Button("Ячейки", () -> {
            filterWindow.close();
            findBinWindow();
        });
        Button payments = new Button("Платежи", () -> {
            filterWindow.close();
            findPaymentWindow();
        });
        Button exit = new Button("Выход", () -> {
            filterWindow.close();
            showMainMenu();
        });

        panel.addComponent(new Label("ВЫБЕРИТЕ СУЩНОСТЬ ДЛЯ ПОИСКА: "));
        panel.addComponent(bin);
        panel.addComponent(payments);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        filterWindow.setComponent(panel);
        gui.addWindowAndWait(filterWindow);
    }

    private static void findBinWindow() {
        BasicWindow findBin = new BasicWindow("Поиск ячейки");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        TextBox filterTypeBox = new TextBox(new TerminalSize(30, 2));
        filterTypeBox.setText("size/status");
        TextBox filterValueBox = new TextBox(new TerminalSize(30, 2));
        filterValueBox.setText("enter value");
        TextBox sortResultBox = new TextBox(new TerminalSize(30, 2));
        sortResultBox.setText("DESC/ASC");

        Button send = new Button("Продолжить", () -> {
            String filterType = filterTypeBox.getText();
            String filterValue = filterValueBox.getText();
            String sortValue = sortResultBox.getText();
            Boolean fT = containsDigit(filterType);
            Boolean fV = containsDigit(filterValue);
            Boolean sV = containsDigit(sortValue);
            // Исправить валидацию входящих данных в фильтры
            // if ((fT == true) || (fV == true) || (sV == true)) {
            //     panel.addComponent(new Label("НЕВЕРНЫЕ ФИЛЬТРЫ. Не используйите числа при фильтрации"));
            // }
            // Дописать SQL запрос
            var DBResponse = DBHelper.getDataFromDB("", filterType, filterValue, sortValue);
        });
        Button exit = new Button("Выход", findBin::close);
        // Отрисовать таблицу и после получения DBResponse заполнить таблицу данными из списка

        panel.addComponent(new Label("ПОИСК ЯЧЕЙКИ"));
        filterPanel.addComponent(filterTypeBox);
        filterPanel.addComponent(filterValueBox);
        filterPanel.addComponent(sortResultBox);
        panel.addComponent(filterPanel);
        panel.addComponent(send);
        //Вот тут должна быть таблица!
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        findBin.setComponent(panel);
        gui.addWindowAndWait(findBin);
    }

    private static void findPaymentWindow() {
        BasicWindow findPayment = new BasicWindow("Поиск платежа");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Label DisplayLabel = new Label("ПОИСК ПЛАТЕЖЕЙ");
    }


}
