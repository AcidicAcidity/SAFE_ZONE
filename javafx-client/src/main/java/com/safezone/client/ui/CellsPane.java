package com.safezone.client.ui;

import com.safezone.client.model.Models.Cell;
import com.safezone.client.model.Models.CellSize;
import com.safezone.client.model.Models.CellStatus;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Cells with their warehouse and physical position. */
public final class CellsPane extends BorderPane {
    private record CellData(List<Warehouse> warehouses, List<Cell> cells) { }

    private final SafeZoneClient client;
    private final Window owner;
    private final TableView<Cell> table = new TableView<>();
    private final Map<Integer, String> warehouseNames = new HashMap<>();

    public CellsPane(SafeZoneClient client, Window owner, Runnable onBack) {
        this.client = client;
        this.owner = owner;
        setId("cells-view");
        setPadding(new Insets(32));
        setTop(Ui.header("Все ячейки", onBack, this::load));
        configureTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(22, 0, 0, 0));
        Platform.runLater(this::load);
    }

    private void configureTable() {
        table.setId("cells-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(46);
        table.setMinHeight(230);
        table.setPlaceholder(Ui.emptyLabel("Ячейки не найдены."));
        TableColumn<Cell, Integer> id = Ui.column("ID", Cell::id);
        TableColumn<Cell, String> warehouse = Ui.column("Склад", this::warehouseName);
        TableColumn<Cell, String> number = Ui.column("Номер", Cell::number);
        TableColumn<Cell, CellSize> size = Ui.column("Размер", Cell::size);
        TableColumn<Cell, CellStatus> status = Ui.column("Статус", Cell::status);
        TableColumn<Cell, Integer> row = Ui.column("Ряд", Cell::row);
        TableColumn<Cell, Integer> column = Ui.column("Колонка", Cell::column);
        id.setPrefWidth(65);
        warehouse.setPrefWidth(250);
        number.setPrefWidth(140);
        size.setPrefWidth(130);
        status.setPrefWidth(150);
        row.setPrefWidth(75);
        column.setPrefWidth(100);
        table.getColumns().addAll(List.of(id, warehouse, number, size, status, row, column));
    }

    private void load() {
        FxTasks.run(this, () -> new CellData(client.warehouses(), client.cells()), data -> {
            warehouseNames.clear();
            data.warehouses().forEach(warehouse -> warehouseNames.put(warehouse.id(), warehouse.name()));
            table.getItems().setAll(data.cells());
            table.sort();
        }, error -> Ui.error(owner, error));
    }

    private String warehouseName(Cell cell) {
        String name = warehouseNames.get(cell.warehouseId());
        return name == null ? "Склад № " + cell.warehouseId() : "№ " + cell.warehouseId() + " · " + name;
    }
}
