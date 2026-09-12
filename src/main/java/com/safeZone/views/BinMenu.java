package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.*;

import com.safeZone.util.DBHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinMenu {
    private static final Logger log = LoggerFactory.getLogger(BinMenu.class);
    private static WindowBasedTextGUI gui;
    private DBHelper dbHelper;
    private AppMenus appMenus;

    public BinMenu(DBHelper dbHelper, AppMenus appMenus) {
        this.dbHelper = dbHelper;
        this.appMenus = appMenus;
    }

    public void binWindow() {
        BasicWindow binList = new BasicWindow("Список ячеек");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        log.info("Открытие окна с поиском ячеек");
        TextBox binSize = new TextBox(new TerminalSize(30, 2));
        binSize.setText("Размер");
        TextBox binNumber = new TextBox(new TerminalSize(30, 2));
        binNumber.setText("Номер ячейки");
        TextBox binStatus = new TextBox(new TerminalSize(30, 2));
        binStatus.setText("Статус ячейки");
        TextBox sort = new TextBox(new TerminalSize(30, 2));
        sort.setText("DESC/ASC");

        filterPanel.addComponent(binSize);
        filterPanel.addComponent(binNumber);
        filterPanel.addComponent(binStatus);
        filterPanel.addComponent(sort);
        Button search = new Button("Найти", () -> {
            String size = binSize.getText();
            int number = Integer.parseInt(binNumber.getText());
            String status = binStatus.getText();
            String sortData = sort.getText();

        //Запрос к БД.
        });
        panel.addComponent(filterPanel);
        panel.addComponent(search);
        Button exit = new Button("Выход", () -> {
            binList.close();
            appMenus.showMainMenu();
            log.info("Выход из окна поиска ячеек\n Возвращаемся в главное меню");
        });
        panel.addComponent(exit);
        //Таблица
    }
}
