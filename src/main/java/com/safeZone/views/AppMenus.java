package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.Window.Hint;

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
            mainWindow.close();
            createExport.getExportFile();
        }));
        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)))
        panel.addComponent(makeFullWidthButton("Выход", () -> {
            mainWindow.close();
            System.exit(0);
        }));


        mainWindow.setComponent(panel);
        gui.addWindow(mainWindow);
    }

    private Button makeFullWidthButton(String text, Runnable action) {
        Button b = new Button(text, action);
        b.setLayoutData(GridLayout.createLayoutData(
            GridLayout.Alignment.FILL, GridLayout.Alignment.CENTER,
            true, false));
        return b;
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
        gui.addWindow(filterWindow);
    }

}
