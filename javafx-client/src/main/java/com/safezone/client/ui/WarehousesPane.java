package com.safezone.client.ui;

import com.safezone.client.model.Models.Warehouse;
import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.util.FxTasks;
import com.safezone.client.util.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

import java.util.List;

/** Warehouse list loaded through the desktop client's connection. */
public final class WarehousesPane extends BorderPane {
    private final SafeZoneClient client;
    private final Window owner;
    private final TableView<Warehouse> table = new TableView<>();

    public WarehousesPane(SafeZoneClient client, Window owner, Runnable onBack) {
        this.client = client;
        this.owner = owner;
        setId("warehouses-view");
        setPadding(new Insets(32));
        setTop(Ui.header("Склады", onBack, this::load));
        configureTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(22, 0, 0, 0));
        Platform.runLater(this::load);
    }

    private void configureTable() {
        table.setId("warehouses-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(46);
        table.setMinHeight(230);
        table.setPlaceholder(Ui.emptyLabel("Склады не найдены."));
        TableColumn<Warehouse, Integer> id = Ui.column("ID", Warehouse::id);
        TableColumn<Warehouse, String> name = Ui.column("Название", Warehouse::name);
        TableColumn<Warehouse, String> address = Ui.column("Адрес", Warehouse::address);
        id.setPrefWidth(90);
        name.setPrefWidth(300);
        address.setPrefWidth(520);
        table.getColumns().addAll(List.of(id, name, address));
    }

    private void load() {
        FxTasks.run(this, client::warehouses, warehouses -> {
            table.getItems().setAll(warehouses);
            table.sort();
        }, error -> Ui.error(owner, error));
    }
}
