package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import com.safeZone.model.Bin;
import com.safeZone.util.SafeZoneService;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinMenu {
    private static final Logger log = LoggerFactory.getLogger(BinMenu.class);
    private final SafeZoneService service;
    private WindowBasedTextGUI gui;
    private AppMenus appMenus;

    public BinMenu(SafeZoneService service) {
        this.service = service;
    }

    public void setAppMenus(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void setGui(WindowBasedTextGUI gui) {
        this.gui = gui;
    }

    public void binWindow() {
        BasicWindow binList = new BasicWindow("Список ячеек");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        log.info("Открытие окна с поиском ячеек");

        TextBox binSize = new TextBox(new TerminalSize(15, 1));
        binSize.setText("Размер");
        TextBox binNumber = new TextBox(new TerminalSize(15, 1));
        binNumber.setText("Номер");
        TextBox binStatus = new TextBox(new TerminalSize(15, 1));
        binStatus.setText("Статус");
        TextBox sort = new TextBox(new TerminalSize(10, 1));
        sort.setText("DESC/ASC");

        TableModel<String> tableModel = new TableModel<>("ID", "Размер", "Номер", "Статус");
        Table<String> table = new Table<>(tableModel.getColumnLabels().toArray(new String[0]));
        table.setTableModel(tableModel);

        // Таблица сразу заполняется всеми ячейками при открытии окна,
        // а после нажатия "Найти" перезаполняется отфильтрованными данными.
        fillBinTable(tableModel, loadAllBins());

        Button search = new Button("Найти", () -> {
            try {
                String size = normalizedOrNull(binSize.getText(), "Размер");
                Integer number = parseIntOrNull(binNumber.getText(), "Номер");
                String status = normalizedOrNull(binStatus.getText(), "Статус");
                String sortData = normalizedOrNull(sort.getText(), "DESC/ASC");

                List<Bin> bins = service.findBins(size, number, status, sortData); //ДОБАВИТЬ
                fillBinTable(tableModel, bins);
            } catch (NumberFormatException e) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Номер ячейки должен быть числом");
            } catch (SQLException e) {
                log.error("Ошибка поиска ячеек", e);
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось выполнить поиск");
            }
        });

        filterPanel.addComponent(binSize);
        filterPanel.addComponent(binNumber);
        filterPanel.addComponent(binStatus);
        filterPanel.addComponent(sort);
        filterPanel.addComponent(search);

        panel.addComponent(new Label("ЯЧЕЙКИ"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(filterPanel);
        panel.addComponent(new EmptySpace());
        panel.addComponent(table);
        panel.addComponent(new EmptySpace());

        Button exit = new Button("Выход", () -> {
            binList.close();
            log.info("Выход из окна поиска ячеек. Возвращаемся в главное меню");
            appMenus.showMainMenu();
        });
        panel.addComponent(exit);

        binList.setComponent(panel);
        gui.addWindow(binList);
    }

    private List<Bin> loadAllBins() {
        try {
            return service.getAllBins(); //ДОБАВИТЬ
        } catch (SQLException e) {
            log.error("Не удалось загрузить список ячеек", e);
            MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось загрузить ячейки");
            return List.of();
        }
    }

    private void fillBinTable(TableModel<String> tableModel, List<Bin> bins) {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        for (Bin bin : bins) {
            tableModel.addRow(
                String.valueOf(bin.getId()),
                bin.getSize(),
                String.valueOf(bin.getNumber()),
                bin.getStatus()
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
