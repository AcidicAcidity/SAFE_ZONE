package com.safezone.client.ui;

import com.safezone.client.util.Ui;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class AdminPane extends VBox {
    public AdminPane(Consumer<String> navigate, Runnable onHome) {
        super(28);
        setId("admin-menu-view");
        setPadding(new Insets(32));
        var header = Ui.header("Панель администратора", onHome, null);
        ((Button) header.lookup("#page-back")).setId("back-home");
        getChildren().add(header);
        GridPane actions = new GridPane();
        actions.setHgap(18);
        actions.setVgap(18);
        for (int column = 0; column < 2; column++) {
            ColumnConstraints width = new ColumnConstraints();
            width.setPercentWidth(50);
            actions.getColumnConstraints().add(width);
        }
        actions.add(Ui.card("admin-statistics", "Статистика по ячейкам", "Количество аренд каждой ячейки.", () -> navigate.accept("admin-statistics")), 0, 0);
        actions.add(Ui.card("admin-users", "Управление аккаунтами", "Блокировка, разблокировка и удаление пользователей.", () -> navigate.accept("admin-users")), 1, 0);
        actions.add(Ui.card("admin-rentals", "Управление бронями", "Список бронирований и отмена активной брони.", () -> navigate.accept("admin-rentals")), 0, 1);
        actions.add(Ui.card("admin-export", "Экспорт базы в XLSX", "Выгрузка данных в файл Excel.", () -> navigate.accept("admin-export")), 1, 1);
        getChildren().add(actions);
    }
}
