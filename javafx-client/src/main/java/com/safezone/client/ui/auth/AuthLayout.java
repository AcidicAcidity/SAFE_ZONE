package com.safezone.client.ui.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

/** Shared presentation for the separate login and registration screens. */
public final class AuthLayout {
    private AuthLayout() { }

    public static Parent create(boolean registration, Parent form, Runnable onLogin,
                                Runnable onRegister, Runnable onExit) {
        ToggleButton loginTab = new ToggleButton("Вход");
        loginTab.setId("auth-tab-login");
        ToggleButton registerTab = new ToggleButton("Регистрация");
        registerTab.setId("auth-tab-register");
        ToggleGroup tabs = new ToggleGroup();
        loginTab.setToggleGroup(tabs);
        registerTab.setToggleGroup(tabs);
        loginTab.setSelected(!registration);
        registerTab.setSelected(registration);
        loginTab.setOnAction(event -> {
            loginTab.setSelected(true);
            onLogin.run();
        });
        registerTab.setOnAction(event -> {
            registerTab.setSelected(true);
            onRegister.run();
        });
        for (ToggleButton tab : new ToggleButton[]{loginTab, registerTab}) {
            tab.getStyleClass().add("auth-tab");
            tab.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tab, Priority.ALWAYS);
        }
        HBox tabBar = new HBox(4, loginTab, registerTab);
        tabBar.getStyleClass().add("auth-tabs");

        Label title = new Label(registration ? "Создать аккаунт" : "Вход в систему");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label(registration
                ? "Укажите логин и пароль для нового аккаунта."
                : "Войдите, чтобы продолжить работу с ячейками и арендой.");
        subtitle.getStyleClass().add("auth-subtitle");
        subtitle.setWrapText(true);
        VBox card = new VBox(20, tabBar, new VBox(8, title, subtitle), form);
        card.getStyleClass().add("auth-card");
        card.setPadding(new Insets(30));
        card.setMaxWidth(480);
        VBox content = new VBox(card);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28, 24, 28, 24));
        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("auth-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Label footer = new Label("SAFEZONE  ·  Система аренды складских ячеек");
        footer.getStyleClass().add("small-text");
        HBox footerBar = new HBox(footer);
        footerBar.setAlignment(Pos.CENTER);
        footerBar.setPadding(new Insets(12, 20, 18, 20));
        BorderPane root = new BorderPane(scroll);
        root.getStyleClass().add("auth-background");
        root.setTop(header(onExit));
        root.setBottom(footerBar);
        return root;
    }

    private static HBox header(Runnable onExit) {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 2 H21 V6 H3 Z M5 6 V23 H19 V6 M9 10 H15 M10 14 H14");
        icon.getStyleClass().add("brand-icon");
        StackPane mark = new StackPane(icon);
        mark.getStyleClass().add("brand-mark");
        Label name = new Label("Аренда ячеек");
        name.getStyleClass().add("brand-title");
        Label wordmark = new Label("SAFEZONE");
        wordmark.getStyleClass().add("brand-subtitle");
        HBox brand = new HBox(12, mark, new VBox(2, name, wordmark));
        brand.setAlignment(Pos.CENTER_LEFT);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        Button exit = new Button("Выйти из приложения");
        exit.setId("app-exit");
        exit.getStyleClass().add("secondary-button");
        exit.setOnAction(event -> onExit.run());
        HBox header = new HBox(16, brand, space, exit);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("app-header");
        return header;
    }
}
