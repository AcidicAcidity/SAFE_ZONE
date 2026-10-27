package com.safezone.client.ui;

import com.safezone.client.model.Models;
import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.util.FxTasks;
import com.safezone.client.util.Ui;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/** Account list and actions, with the same structure as the reference UsersPane. */
public final class UsersPane extends BorderPane {
    private final SafeZoneClient client;
    private final Window owner;
    private final TableView<Models.User> table = new TableView<>();
    private final Label selection = new Label("Пользователь не выбран");
    private final Button block = new Button("Заблокировать");
    private final Button unblock = new Button("Разблокировать");
    private final Button delete = new Button("Удалить аккаунт");

    public UsersPane(SafeZoneClient client, Window owner, Runnable onBack) {
        this.client = client;
        this.owner = owner;
        setId("users-view");
        setPadding(new Insets(32));
        setTop(Ui.header("Управление аккаунтами", onBack, this::load));
        configureTable();
        block.setId("user-block");
        unblock.setId("user-unblock");
        delete.setId("user-delete");
        block.getStyleClass().add("secondary-button");
        unblock.getStyleClass().add("secondary-button");
        delete.getStyleClass().add("danger-button");
        block.setOnAction(event -> changeStatus(Models.UserStatus.BLOCKED));
        unblock.setOnAction(event -> changeStatus(Models.UserStatus.ACTIVE));
        delete.setOnAction(event -> changeStatus(Models.UserStatus.DELETED));
        VBox actions = new VBox(16, selection, new HBox(10, block, unblock, delete));
        actions.setPadding(new Insets(20));
        actions.getStyleClass().add("surface-card");
        VBox body = new VBox(20, table, actions);
        BorderPane.setMargin(body, new Insets(20, 0, 0, 0));
        setCenter(body);
        updateSelection(null);
        Platform.runLater(this::load);
    }

    private void configureTable() {
        table.setId("users-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(41);
        table.setPrefHeight(300);
        table.setPlaceholder(Ui.emptyLabel("Пользователи не найдены."));
        table.getColumns().addAll(List.of(Ui.column("ID", Models.User::id), Ui.column("Логин", Models.User::login),
                Ui.column("Роль", Models.User::role), Ui.column("Статус", Models.User::status)));
        table.getSelectionModel().selectedItemProperty().addListener((observable, previous, user) -> updateSelection(user));
    }

    private void updateSelection(Models.User user) {
        selection.setText(user == null ? "Пользователь не выбран" : "Пользователь № " + user.id() + " · " + user.login());
        block.setDisable(user == null || user.status() == Models.UserStatus.BLOCKED);
        unblock.setDisable(user == null || user.status() == Models.UserStatus.ACTIVE);
        delete.setDisable(user == null || user.status() == Models.UserStatus.DELETED);
    }

    private void load() {
        FxTasks.run(this, client::users, values -> {
            table.setItems(FXCollections.observableArrayList(values));
            updateSelection(null);
        }, error -> Ui.error(owner, error));
    }

    private void changeStatus(Models.UserStatus status) {
        Models.User user = table.getSelectionModel().getSelectedItem();
        if (user == null || status == Models.UserStatus.DELETED
                && !Ui.confirm(owner, "Удалить аккаунт «" + user.login() + "»?")) return;
        FxTasks.mutate(this, () -> {
            switch (status) {
                case ACTIVE -> client.unblockUser(user.id());
                case BLOCKED -> client.blockUser(user.id());
                case DELETED -> client.deleteUser(user.id());
            }
            return null;
        }, ignored -> load(), error -> Ui.error(owner, error));
    }
}
