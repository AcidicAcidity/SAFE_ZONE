package com.safezone.client.ui;

import com.safezone.client.model.Models;
import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.util.FxTasks;
import com.safezone.client.util.Ui;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Main navigation with self-contained section panes, following the example. */
public final class MainView extends BorderPane {
    private final SafeZoneClient client;
    private final Window owner;
    private final ScrollPane content = new ScrollPane();

    public MainView(SafeZoneClient client, Window owner, Runnable logout) {
        this.client = client;
        this.owner = owner;
        setId("main-view");
        getStyleClass().add("main-background");
        setTop(buildHeader(logout));
        content.setFitToWidth(true);
        content.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        content.getStyleClass().add("page-scroll");
        setCenter(content);
        HBox footer = new HBox(Ui.emptyLabel("SAFEZONE"));
        footer.getStyleClass().add("app-footer");
        setBottom(footer);
        showHome();
    }

    private HBox buildHeader(Runnable logout) {
        Models.User user = client.currentUser();
        SVGPath icon = new SVGPath();
        icon.setContent("M3 2 H21 V6 H3 Z M5 6 V23 H19 V6 M9 10 H15");
        icon.getStyleClass().add("brand-icon");
        StackPane mark = new StackPane(icon);
        mark.getStyleClass().add("brand-mark");
        Label brand = new Label("Аренда ячеек");
        brand.getStyleClass().add("brand-title");
        Button home = new Button("Главная");
        home.setId("nav-home");
        home.getStyleClass().add("nav-button");
        home.setOnAction(event -> showHome());
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        MenuButton account = new MenuButton(user.login() + " · " + user.role());
        account.setId("account-menu");
        account.setMnemonicParsing(false);
        account.getStyleClass().add("account-menu");
        MenuItem identity = new MenuItem("ID: " + user.id() + " · " + user.status());
        identity.setDisable(true);
        MenuItem signOut = new MenuItem("Выйти из аккаунта");
        signOut.setId("account-logout");
        signOut.setOnAction(event -> logout.run());
        MenuItem exit = new MenuItem("Завершить программу");
        exit.setId("app-exit");
        exit.setOnAction(event -> Platform.exit());
        account.getItems().addAll(identity, new SeparatorMenuItem(), signOut, exit);
        HBox header = new HBox(28, mark, brand, home, space, account);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("app-header");
        return header;
    }

    private void showHome() { show(new HomePane(client.currentUser(), this::open)); }
    private void showAdmin() { show(new AdminPane(this::open, this::showHome)); }

    private void show(Node pane) {
        content.setContent(pane);
        content.setVvalue(0);
    }

    private void open(String destination) {
        if (destination.startsWith("admin") && !client.currentUser().isAdmin()) return;
        switch (destination) {
            case "warehouses" -> show(new WarehousesPane(client, owner, this::showHome));
            case "cells" -> show(new CellsPane(client, owner, this::showHome));
            case "rent" -> show(new RentPane(client, owner, this::showHome, () -> open("my-rentals")));
            case "my-rentals" -> show(new RentalsPane(client, owner, false, this::showHome));
            case "admin" -> showAdmin();
            case "admin-statistics" -> show(new CellStatisticsPane(client, owner, this::showAdmin));
            case "admin-users" -> show(new UsersPane(client, owner, this::showAdmin));
            case "admin-rentals" -> show(new RentalsPane(client, owner, true, this::showAdmin));
            case "admin-export" -> exportDatabase();
            default -> throw new IllegalArgumentException("Unknown screen: " + destination);
        }
    }

    private void exportDatabase() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Экспорт базы в XLSX");
        chooser.setInitialFileName("safezone-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = chooser.showSaveDialog(owner);
        if (file != null) FxTasks.mutate(this, () -> client.exportDatabase(file.toPath()),
                saved -> Ui.info(owner, "Файл сохранён: " + saved.getFileName()), error -> Ui.error(owner, error));
    }
}
