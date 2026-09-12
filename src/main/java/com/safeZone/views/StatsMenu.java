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

public class StatsMenu {
    private Logger log = LoggerFactory.getLogger(StatsMenu.class);
    private static WindowBasedTextGUI gui;
    private DBHelper dbHelper;
    private AppMenus appMenus;

    public StatsMenu(DBHelper dbHelper, AppMenus appMenus) {
        this.dbHelper = dbHelper;
        this.appMenus = appMenus;
    }

    public void showStats() {
        BasicWindow statsWindow = new BasicWindow("Статистика ячеек");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        // тут запрос в БД где мы берем ячейку и пробиваем все платежи по этой ячейке.
        // Отдельно ищем самую длинную аренду и текущий статус ячейки
        //
        gui.addWindow(statsWindow);
    }
}
