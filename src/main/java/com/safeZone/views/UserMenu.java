package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import com.safeZone.model.UserRecord;
import com.safeZone.util.SafeZoneService;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMenu {
    private static final Logger log = LoggerFactory.getLogger(UserMenu.class);
    private final SafeZoneService service;
    private WindowBasedTextGUI gui;
    private AppMenus appMenus;

    public UserMenu(SafeZoneService service) {
        this.service = service;
    }

    public void setAppMenus(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void setGui(WindowBasedTextGUI gui) {
        this.gui = gui;
    }

    public void findUser() {
        log.info("Open find user window");
        BasicWindow userWindow = new BasicWindow("Поиск пользователя");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        TextBox userId = new TextBox(new TerminalSize(10, 1));
        userId.setText("ID");
        TextBox userStatus = new TextBox(new TerminalSize(20, 1));
        userStatus.setText("активен/заблокирован");
        TextBox userRole = new TextBox(new TerminalSize(24, 1));
        userRole.setText("Пользователь/Администратор");
        TextBox sortResult = new TextBox(new TerminalSize(10, 1));
        sortResult.setText("DESC/ASC");

        TableModel<String> tableModel = new TableModel<>("ID", "Имя", "Статус", "Роль");
        Table<String> table = new Table<>(tableModel.getColumnLabels().toArray(new String[0]));
        table.setTableModel(tableModel);

        // Таблица сразу заполняется всеми пользователями при открытии,
        // фильтр только перезаполняет её результатами поиска.
        fillUserTable(tableModel, loadAllUsers());

        Button searchButton = new Button("Поиск", () -> {
            try {
                Integer id = parseIntOrNull(userId.getText(), "ID");
                String status = normalizedOrNull(userStatus.getText(), "активен/заблокирован");
                String role = normalizedOrNull(userRole.getText(), "Пользователь/Администратор");
                String sort = normalizedOrNull(sortResult.getText(), "DESC/ASC");

                List<UserRecord> users = service.findUsers(id, status, role, sort); //ДОБАВИТЬ
                fillUserTable(tableModel, users);
            } catch (NumberFormatException e) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "ID должен быть числом");
            } catch (SQLException e) {
                log.error("Ошибка поиска пользователей", e);
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось выполнить поиск");
            }
        });

        filterPanel.addComponent(userId);
        filterPanel.addComponent(userStatus);
        filterPanel.addComponent(userRole);
        filterPanel.addComponent(sortResult);
        filterPanel.addComponent(searchButton);

        panel.addComponent(new Label("ПОИСК ПОЛЬЗОВАТЕЛЯ"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(filterPanel);
        panel.addComponent(new EmptySpace());
        panel.addComponent(table);
        panel.addComponent(new EmptySpace());

        Button exit = new Button("Выход", () -> {
            log.info("Exit");
            userWindow.close();
            appMenus.showMainMenu();
        });
        panel.addComponent(exit);

        userWindow.setComponent(panel);
        gui.addWindow(userWindow);
    }

    private List<UserRecord> loadAllUsers() {
        try {
            return service.getAllUsers(); //ДОБАВИТЬ
        } catch (SQLException e) {
            log.error("Не удалось загрузить список пользователей", e);
            MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось загрузить пользователей");
            return List.of();
        }
    }

    private void fillUserTable(TableModel<String> tableModel, List<UserRecord> users) {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        for (UserRecord u : users) {
            tableModel.addRow(
                String.valueOf(u.getId()),
                u.getUsername(),
                u.getStatus(),
                u.getRole()
            );
        }
    }

    private String normalizedOrNull(String text, String placeholder) {
        if (text == null) return null;
        String trimmed = text.trim();
        return (trimmed.isEmpty() || trimmed.equals(placeholder)) ? null : trimmed;
    }

    private Integer parseIntOrNull(String text, String placeholder) {
        String v = normalizedOrNull(text, placeholder);
        return v == null ? null : Integer.parseInt(v);
    }
}
