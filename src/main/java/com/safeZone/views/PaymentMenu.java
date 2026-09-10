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

public class PaymentMenu {
    private static WindowBasedTextGUI gui;
    private DBHelper dbHelper;
    private AppMenus appMenus;
    private Logger log = LoggerFactory.getLogger(PaymentMenu.class);

    public PaymentMenu(DBHelper dbHelper, AppMenus appMenus) {
        this.dbHelper = dbHelper;
        this.appMenus = appMenus;
    }

    public void paymentWindow() {
        BasicWindow paymentWindow = new BasicWindow("Платежи");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        TextBox binId = new TextBox(new TerminalSize(30, 2));
        binId.setText("Bin ID");
        TextBox ownerId = new TextBox(new TerminalSize(30, 2));
        ownerId.setText("Owner ID");
        TextBox amount = new TextBox(new TerminalSize(30, 2));
        amount.setText("Amount");
        TextBox rentTime = new TextBox(new TerminalSize(30, 2));
        rentTime.setText("Rent Time");
        TextBox status = new TextBox(new TerminalSize(30, 2));
        status.setText("Status");

        Button search = new Button("Поиск", () -> {
            int binIdValue = Integer.parseInt(binId.getText());
            int ownerIdValue = Integer.parseInt(ownerId.getText());
            int amountValue = Integer.parseInt(amount.getText());
            int rentTimeValue = Integer.parseInt(rentTime.getText());
            String statusValue = status.getText();
            // TODO: БД поиск платежей
        });

        filterPanel.addComponent(binId);
        filterPanel.addComponent(ownerId);
        filterPanel.addComponent(amount);
        filterPanel.addComponent(rentTime);
        filterPanel.addComponent(status);
        filterPanel.addComponent(search);
        panel.addComponent(filterPanel);

        paymentWindow.setComponent(panel);
        gui.addWindowAndWait(paymentWindow);
    }
}
