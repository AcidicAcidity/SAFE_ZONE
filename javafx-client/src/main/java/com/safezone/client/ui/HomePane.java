package com.safezone.client.ui;

import com.safezone.client.model.Models;
import com.safezone.client.util.Ui;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/** The same actions as the console menu, presented as desktop navigation. */
public final class HomePane extends VBox {
    public HomePane(Models.User user, Consumer<String> navigate) {
        super(28);
        setId("main-menu-view");
        setPadding(new Insets(32));
        Label title = new Label("Главная");
        title.getStyleClass().add("page-title");
        getChildren().add(new VBox(10, title, Ui.emptyLabel("Выберите действие")));

        GridPane actions = new GridPane();
        actions.setHgap(18);
        actions.setVgap(18);
        for (int column = 0; column < 2; column++) {
            ColumnConstraints width = new ColumnConstraints();
            width.setPercentWidth(50);
            actions.getColumnConstraints().add(width);
        }
        actions.add(Ui.card("menu-warehouses", "Склады", "Список складов и их адреса.", () -> navigate.accept("warehouses")), 0, 0);
        actions.add(Ui.card("menu-cells", "Все ячейки", "Ячейки складов, их размеры и доступность.", () -> navigate.accept("cells")), 1, 0);
        actions.add(Ui.card("menu-rent", "Арендовать", "Выбор ячейки или размера и срока аренды.", () -> navigate.accept("rent")), 0, 1);
        actions.add(Ui.card("menu-my-rentals", "Мои аренды", "Ваши бронирования и сроки хранения.", () -> navigate.accept("my-rentals")), 1, 1);
        getChildren().add(actions);
        if (user.isAdmin()) {
            Button admin = Ui.card("menu-admin", "Панель администратора", "Статистика, аккаунты и бронирования.", () -> navigate.accept("admin"));
            admin.getStyleClass().add("admin-navigation-card");
            getChildren().add(admin);
        }
    }
}
