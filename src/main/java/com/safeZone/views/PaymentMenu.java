package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;

import com.safeZone.model.Payment;
import com.safeZone.util.SafeZoneService;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentMenu {
    private static final Logger log = LoggerFactory.getLogger(PaymentMenu.class);
    private final SafeZoneService service;
    private WindowBasedTextGUI gui;
    private AppMenus appMenus;

    public PaymentMenu(SafeZoneService service ) {
        this.service = service;
    }

    public void setAppMenus(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void setGui(WindowBasedTextGUI gui) {
        this.gui = gui;
    }

    public void paymentWindow() {
        BasicWindow paymentWindow = new BasicWindow("Платежи");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        TextBox binId = new TextBox(new TerminalSize(10, 1));
        binId.setText("Bin ID");
        TextBox ownerId = new TextBox(new TerminalSize(10, 1));
        ownerId.setText("Owner ID");
        TextBox amount = new TextBox(new TerminalSize(10, 1));
        amount.setText("Amount");
        TextBox rentTime = new TextBox(new TerminalSize(10, 1));
        rentTime.setText("Rent Time");
        TextBox status = new TextBox(new TerminalSize(10, 1));
        status.setText("Status");

        TableModel<String> tableModel = new TableModel<>(
            "ID", "Bin ID", "Owner ID", "Amount", "Rent Time", "Status");
        Table<String> table = new Table<>(tableModel.getColumnLabels().toArray(new String[0]));
        table.setTableModel(tableModel);

        // Таблица сразу заполняется всеми платежами, фильтр только перезаполняет её.
        fillPaymentTable(tableModel, loadAllPayments());

        Button search = new Button("Поиск", () -> {
            try {
                Integer binIdValue = parseIntOrNull(binId.getText(), "Bin ID");
                Integer ownerIdValue = parseIntOrNull(ownerId.getText(), "Owner ID");
                Integer amountValue = parseIntOrNull(amount.getText(), "Amount");
                Integer rentTimeValue = parseIntOrNull(rentTime.getText(), "Rent Time");
                String statusValue = normalizedOrNull(status.getText(), "Status");

                List<Payment> payments = service.findPayments( //ДОДЕЛАТЬ
                    binIdValue, ownerIdValue, amountValue, rentTimeValue, statusValue);
                fillPaymentTable(tableModel, payments);
            } catch (NumberFormatException e) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Числовые поля должны содержать число");
            } catch (SQLException e) {
                log.error("Ошибка поиска платежей", e);
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось выполнить поиск");
            }
        });

        filterPanel.addComponent(binId);
        filterPanel.addComponent(ownerId);
        filterPanel.addComponent(amount);
        filterPanel.addComponent(rentTime);
        filterPanel.addComponent(status);
        filterPanel.addComponent(search);

        panel.addComponent(new Label("ПЛАТЕЖИ"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(filterPanel);
        panel.addComponent(new EmptySpace());
        panel.addComponent(table);
        panel.addComponent(new EmptySpace());

        Button exit = new Button("Выход", () -> {
            paymentWindow.close();
            appMenus.showMainMenu();
        });
        panel.addComponent(exit);

        paymentWindow.setComponent(panel);
        gui.addWindow(paymentWindow);
    }

    private List<Payment> loadAllPayments() {
        try {
            return service.getAllPayments(); //ДОБАВИТЬ
        } catch (SQLException e) {
            log.error("Не удалось загрузить список платежей", e);
            MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось загрузить платежи");
            return List.of();
        }
    }

    private void fillPaymentTable(TableModel<String> tableModel, List<Payment> payments) {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        for (Payment p : payments) {
            tableModel.addRow(
                String.valueOf(p.getId()),
                String.valueOf(p.getBinId()),
                String.valueOf(p.getOwnerId()),
                String.valueOf(p.getAmount()),
                String.valueOf(p.getRentTime()),
                p.getStatus()
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
