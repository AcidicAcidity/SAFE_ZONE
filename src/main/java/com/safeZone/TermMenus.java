package com.safeZone;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;


public class TermMenus {

    private static WindowBasedTextGUI gui;

    public static void main() throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        gui = new MultiWindowTextGUI(screen);

        showMainMenu();

        screen.stopScreen();
    }

    private static void showMainMenu() {
        BasicWindow mainWindow = new BasicWindow("Главное меню");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button rent = new Button("Зарегестрировать аренду", () -> {
            mainWindow.close();
            showSizeWindow();
        });
        Button filter = new Button("Найти ячейку", () -> {
            mainWindow.close();
            showFilterWindow();
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

        Label DisplayLabel = new Label("ВЫБЕРИТЕ РАЗМЕР ЯЧЕЙКИ");

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

        panel.addComponent(DisplayLabel);
        panel.addComponent(small);
        panel.addComponent(middle);
        panel.addComponent(big);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        sizeWindow.setComponent(panel);
        gui.addWindowAndWait(sizeWindow);
    }


}
