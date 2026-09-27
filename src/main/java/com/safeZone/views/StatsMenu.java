package com.safeZone.views;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import com.safeZone.model.BinStats;
import com.safeZone.util.SafeZoneService;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsMenu {
    private static final Logger log = LoggerFactory.getLogger(StatsMenu.class);
    private final SafeZoneService service;
    private WindowBasedTextGUI gui;
    private AppMenus appMenus;

    public StatsMenu(SafeZoneService service) {
        this.service = service;
    }

    public void setAppMenus(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void setGui(WindowBasedTextGUI gui) {
        this.gui = gui;
    }

    public void showStats() {
        BasicWindow statsWindow = new BasicWindow("Статистика ячеек");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        TableModel<String> tableModel = new TableModel<>(
            "Ячейка", "Текущий статус", "Кол-во аренд", "Самая долгая аренда (ч)");
        Table<String> table = new Table<>(tableModel.getColumnLabels().toArray(new String[0]));
        table.setTableModel(tableModel);

        // По каждой ячейке берём её платежи, считаем их количество и находим
        // самую долгую аренду; dbHelper.getBinStats() должен отдавать уже готовый агрегат.
        try {
            List<BinStats> stats = service.getBinStats(); //ДОБАВИТЬ
            for (BinStats s : stats) {
                tableModel.addRow(
                    String.valueOf(s.getBinNumber()),
                    s.getCurrentStatus(),
                    String.valueOf(s.getRentCount()),
                    String.valueOf(s.getLongestRentHours())
                );
            }
        } catch (SQLException e) {
            log.error("Не удалось загрузить статистику", e);
            MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось загрузить статистику");
        }

        panel.addComponent(new Label("СТАТИСТИКА ЯЧЕЕК"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(table);
        panel.addComponent(new EmptySpace());

        Button exit = new Button("Выход", () -> {
            statsWindow.close();
            appMenus.showMainMenu();
        });
        panel.addComponent(exit);

        statsWindow.setComponent(panel);
        gui.addWindow(statsWindow);
    }
}
