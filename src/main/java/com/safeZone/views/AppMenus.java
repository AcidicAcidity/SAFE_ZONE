package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.safeZone.util.DBHelper;
import com.safeZone.util.createExport;

import java.util.*;

import com.googlecode.lanterna.gui2.table.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.*;

public class AppMenus {

    private static final Logger log = LoggerFactory.getLogger(AppMenus.class);
    private DBHelper dbHelper;
    private createExport createExport;
    private static WindowBasedTextGUI gui;

    private final RentMenu rentMenu;
    private final BinMenu binMenu;
    private final PaymentMenu paymentMenu;
    private final UserMenu userMenu;
    private final StatsMenu statsMenu;

    public AppMenus(RentMenu rentMenu, BinMenu binMenu, PaymentMenu paymentMenu, UserMenu userMenu, StatsMenu statsMenu) {
        this.rentMenu = rentMenu;
        this.binMenu = binMenu;
        this.paymentMenu = paymentMenu;
        this.userMenu = userMenu;
        this.statsMenu = statsMenu;
    }

    public void start() throws Exception {
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

    public void showMainMenu() {
        BasicWindow mainWindow = new BasicWindow("Главное меню");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button rent = new Button("Зарегестрировать аренду", () -> {
            mainWindow.close();
            rentMenu.showRentMenu();
        });
        Button filter = new Button("Найти сущность (ячейка/платеж)", () -> {
            mainWindow.close();
            showFilterWindow();
        });
        Button userSearch = new Button("Список пользователей", () -> {
            mainWindow.close();
            userMenu.findUser();
        });
        Button stats = new Button("Статистика ячеек", () -> {
            mainWindow.close();
            statsMenu.showStats();
        });
        Button export = new Button("Экспорт данных", () -> {
            mainWindow.close();
            createExport.getExportFile();
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


    private void showFilterWindow() {
        BasicWindow filterWindow = new BasicWindow("Поиск ячейки или платежа");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button bin = new Button("Ячейки", () -> {
            filterWindow.close();
            binMenu.binWindow();
        });
        Button payments = new Button("Платежи", () -> {
            filterWindow.close();
            paymentMenu.paymentWindow();
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

}
