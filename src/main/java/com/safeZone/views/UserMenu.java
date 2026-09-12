package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.*;

import com.safeZone.util.DBHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMenu {
    private Logger log = LoggerFactory.getLogger(UserMenu.class);
    private static WindowBasedTextGUI gui;
    private DBHelper dbHelper;
    private AppMenus appMenus;

    public UserMenu(DBHelper dbHelper, AppMenus appMenus) {
        this.dbHelper = dbHelper;
        this.appMenus = appMenus;
    }

    public void findUser() {
        log.info("Open find user window");
        BasicWindow userWindow = new BasicWindow("ПОИСК ПОЛЬЗОВАТЕЛЯ");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        TextBox userId = new TextBox(new TerminalSize(30, 2));
        userId.setText("1");
        TextBox userStatus = new TextBox(new TerminalSize(30, 2));
        userStatus.setText("активен/заблокирован");
        TextBox userRole = new TextBox(new TerminalSize(30, 2));
        userRole.setText("Пользователь/Администратор");
        TextBox sortResult = new TextBox(new TerminalSize(30, 2));
        sortResult.setText("DESC/ASC");

        filterPanel.addComponent(userId);
        filterPanel.addComponent(userStatus);
        filterPanel.addComponent(userRole);

        Button searchButton = new Button("Поиск", () -> {
            int id = Integer.parseInt(userId.getText());
            String status = userStatus.getText();
            String role = userRole.getText();
            String sort = sortResult.getText();
            // TODO: Дописать запрос в БД для поиска пользователя по id, status и role
        });

        Button exit = new Button("Выход", () -> {
            log.info("Exit");
            userWindow.close();
            appMenus.showMainMenu();
        });
        panel.addComponent(new Label("ПОИСК ПОЛЬЗОВАТЕЛЯ"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(filterPanel);
        panel.addComponent(new EmptySpace());
        panel.addComponent(searchButton);
        // Тут таблицу делаем, предварительно взяв из БД абсолютно всех пользователей
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        gui.addWindow(userWindow);

    }
}
