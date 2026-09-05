package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.safeZone.util.DBHelper;

import java.util.*;

import com.googlecode.lanterna.gui2.table.Table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.*;

public class TermMenus {

    private static final Logger log = LoggerFactory.getLogger(TermMenus.class);
    private final DBHelper dbHelper;
    private static WindowBasedTextGUI gui;

    public TermMenus(DBHelper dbHelper){
        this.dbHelper = dbHelper;
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

    private void showMainMenu() {
        BasicWindow mainWindow = new BasicWindow("Главное меню");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button rent = new Button("Зарегестрировать аренду", () -> {
            mainWindow.close();
            showSizeWindow();
        });
        Button filter = new Button("Найти сущность (ячейка/платеж)", () -> {
            mainWindow.close();
            showFilterWindow();
        });
        Button userSearch = new Button("Список пользователей", () -> {
            mainWindow.close();
            findUserWindow();
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

    private void showSizeWindow() {
        BasicWindow sizeWindow = new BasicWindow("Аренда");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

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

        panel.addComponent(new Label("ВЫБЕРИТЕ РАЗМЕР ЯЧЕЙКИ"));
        panel.addComponent(small);
        panel.addComponent(middle);
        panel.addComponent(big);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        sizeWindow.setComponent(panel);
        gui.addWindowAndWait(sizeWindow);
    }

    private void showFilterWindow() {
        BasicWindow filterWindow = new BasicWindow("Поиск ячейки или платежа");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Label DisplayLabel = new Label("ВЫБЕРИТЕ СУЩНОСТЬ ДЛЯ ПОИСКА: ");

        Button bin = new Button("Ячейки", () -> {
            filterWindow.close();
            findBinWindow();
        });
        Button payments = new Button("Платежи", () -> {
            filterWindow.close();
            findPaymentWindow();
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
        gui.addWindowAndWait(filterWindow);
    }

    private void findBinWindow() {
        BasicWindow findBin = new BasicWindow("Поиск ячейки");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel filterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        TextBox filterTypeBox = new TextBox(new TerminalSize(30, 2));
        filterTypeBox.setText("size/status");
        TextBox filterValueBox = new TextBox(new TerminalSize(30, 2));
        filterValueBox.setText("enter value");
        TextBox sortResultBox = new TextBox(new TerminalSize(30, 2));
        sortResultBox.setText("DESC/ASC");
        //это вот таблица для результатов
        Table<String> resultTable = new Table<>("ID","Price","Position","Status");
        resultTable.setVisible(false);

        //Крч тут я сделал валидацию

        Button send = new Button("Продолжить", () -> {
            String filterType = filterTypeBox.getText();
            String filterValue = filterValueBox.getText();
            String sortValue = sortResultBox.getText();

            boolean valid = true;
            StringBuilder errorMsg = new StringBuilder();

            if (!filterType.isEmpty() && !filterValue.isEmpty()){
                if (filterType.equalsIgnoreCase("Size")){
                    try{
                        Integer.parseInt(filterValue);
                    }catch (NumberFormatException e){
                        valid = false;
                        errorMsg.append("Для size нужно целое число");
                    }
                }else if (filterType.equalsIgnoreCase("status_bins")|| filterType.equalsIgnoreCase("status")){
                    List<String> allowed = Arrays.asList("available", "rented", "maintenance", "broken");
                    if (!allowed.contains(filterValue.toLowerCase())){
                        valid = false;
                         errorMsg.append("Допустимые статусы: available, rented, maintenance, broken. ");
                    }
                }else{
                    valid = false;
                    errorMsg.append("Фильтр может быть только 'size' или 'status_bins'. ");
                }
            }
            // тут мы проверяем сортировку
            if (!sortValue.isEmpty() && !"ASC".equals(sortValue) && !"DESC".equals(sortValue)) {
                valid = false;
                errorMsg.append("Сортировка только ASC или DESC. ");
            }
             if (!valid){
                 System.err.println("ОШИБКА: " + errorMsg.toString());
                 return;
             }
             //Тут начинаем формировать наши скьюл запросики
             StringBuilder sql = new StringBuilder("SELECT * FROM bins WHERE 1=1");
             List<Object> params = new ArrayList<>();

             if (filterType.equalsIgnoreCase("size") && !filterValue.isEmpty()) {
                sql.append(" AND size = ?");
                params.add(Integer.parseInt(filterValue));
             } else if ((filterType.equalsIgnoreCase("status_bins") || filterType.equalsIgnoreCase("status")) && !filterValue.isEmpty()){
                sql.append(" AND status_bins = ?");
                params.add(filterValue);
             }
             //а вот конкретно тут сортируем уже
             if ("ASC".equals(sortValue)|| "DESC".equals(sortValue)){
                sql.append(" ORDER BY id ").append(sortValue);
             }else{
                sql.append(" ORDER BY id ASC");
             }

            //через DBHelper запрос делаем
            List<Map<String, Object>> rows = null;
            try{
                rows = dbHelper.getDataFromDB(sql.toString(),params.toArray());
            }catch (SQLException e){
                log.info("NULL RESPONSE DB: " + e);
                return;
            }

            // заполняем нашу табличку
            resultTable.getTableModel().clear();
            if (rows != null && !rows.isEmpty()){
                for (Map<String, Object> row : rows) {
                    resultTable.getTableModel().addRow(new String[]{
                        String.valueOf(row.get("id")),
                        String.valueOf(row.get("price")),
                        String.valueOf(row.get("pos_x")),
                        String.valueOf(row.get("pos_y")),
                        String.valueOf(row.get("size")),
                        String.valueOf(row.get("status")),
                    });
                }
                if (!panel.getChildren().contains(resultTable)){
                    panel.addComponent(resultTable);
                }
            }else{
                panel.removeComponent(resultTable);
            }
            findBin.invalidate();

        });
        Button exit = new Button("Выход", findBin::close);
        // Отрисовать таблицу и после получения DBResponse заполнить таблицу данными из списка

        panel.addComponent(new Label("ПОИСК ЯЧЕЙКИ"));
        filterPanel.addComponent(filterTypeBox);
        filterPanel.addComponent(filterValueBox);
        filterPanel.addComponent(sortResultBox);
        panel.addComponent(filterPanel);
        panel.addComponent(send);
        //Вот тут должна быть таблица!
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        findBin.setComponent(panel);
        gui.addWindowAndWait(findBin);
    }

    private void findPaymentWindow() {
        BasicWindow findPayment = new BasicWindow("Поиск платежа");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Label DisplayLabel = new Label("ПОИСК ПЛАТЕЖЕЙ");
    }

}
