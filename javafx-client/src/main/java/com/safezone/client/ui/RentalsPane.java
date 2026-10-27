package com.safezone.client.ui;

import com.safezone.client.model.Models.Cell;
import com.safezone.client.model.Models.CellSize;
import com.safezone.client.model.Models.Rental;
import com.safezone.client.model.Models.RentalStatus;
import com.safezone.client.model.Models.Warehouse;
import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.util.FxTasks;
import com.safezone.client.util.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Own rentals, or all bookings with administrator cancellation. */
public final class RentalsPane extends BorderPane {
    private record RentalData(List<Rental> rentals, List<Cell> cells, List<Warehouse> warehouses) { }

    private final SafeZoneClient client;
    private final Window owner;
    private final boolean administration;
    private final TableView<Rental> table = new TableView<>();
    private final Map<Integer, Cell> cellsById = new HashMap<>();
    private final Map<Integer, Warehouse> warehousesById = new HashMap<>();

    public RentalsPane(SafeZoneClient client, Window owner, boolean administration, Runnable onBack) {
        this.client = client;
        this.owner = owner;
        this.administration = administration;
        setId("rentals-view");
        setPadding(new Insets(32));
        setTop(Ui.header(administration ? "Управление бронями" : "Мои аренды", onBack, this::load));
        configureTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(22, 0, 0, 0));
        if (administration) configureCancellation();
        Platform.runLater(this::load);
    }

    private void configureTable() {
        table.setId("rentals-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(46);
        table.setMinHeight(230);
        table.setPlaceholder(Ui.emptyLabel(administration ? "Броней пока нет." : "У вас пока нет аренд."));
        TableColumn<Rental, Integer> id = Ui.column("№ аренды", Rental::id);
        id.setPrefWidth(95);
        table.getColumns().add(id);
        if (administration) {
            TableColumn<Rental, Integer> user = Ui.column("ID пользователя", Rental::userId);
            user.setPrefWidth(135);
            table.getColumns().add(user);
        }
        TableColumn<Rental, String> warehouse = Ui.column("Склад", this::warehouseName);
        TableColumn<Rental, String> cell = Ui.column("Ячейка", this::cellNumber);
        TableColumn<Rental, CellSize> size = Ui.column("Размер", this::cellSize);
        TableColumn<Rental, RentalStatus> status = Ui.column("Статус", Rental::status);
        warehouse.setPrefWidth(170);
        cell.setPrefWidth(115);
        size.setPrefWidth(115);
        status.setPrefWidth(135);
        table.getColumns().addAll(List.of(warehouse, cell, size,
                dateColumn("Начало", true), dateColumn("Окончание", false), status));
    }

    private void configureCancellation() {
        Button cancel = new Button("Отменить бронь");
        cancel.setId("rental-cancel");
        cancel.getStyleClass().add("danger-button");
        cancel.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selected) ->
                cancel.setDisable(selected == null || selected.status() != RentalStatus.ACTIVE));
        cancel.setOnAction(event -> cancelSelected());
        HBox actions = new HBox(cancel);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(18, 0, 0, 0));
        setBottom(actions);
    }

    private void load() {
        FxTasks.run(this, () -> new RentalData(administration ? client.rentals() : client.myRentals(),
                client.cells(), client.warehouses()), data -> {
            cellsById.clear();
            warehousesById.clear();
            data.cells().forEach(cell -> cellsById.put(cell.id(), cell));
            data.warehouses().forEach(warehouse -> warehousesById.put(warehouse.id(), warehouse));
            table.getItems().setAll(data.rentals());
            table.sort();
        }, error -> Ui.error(owner, error));
    }

    private void cancelSelected() {
        Rental selected = table.getSelectionModel().getSelectedItem();
        if (selected == null || selected.status() != RentalStatus.ACTIVE) return;
        if (!Ui.confirm(owner, "Отменить бронь № " + selected.id() + "?")) return;
        FxTasks.mutate(this, () -> {
            client.cancelRental(selected.id());
            return null;
        }, ignored -> load(), error -> Ui.error(owner, error));
    }

    private TableColumn<Rental, LocalDateTime> dateColumn(String title, boolean start) {
        TableColumn<Rental, LocalDateTime> column = Ui.column(title,
                rental -> start ? rental.startDateTime() : rental.endDateTime());
        column.setPrefWidth(175);
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : Ui.dateTime(value));
            }
        });
        return column;
    }

    private String warehouseName(Rental rental) {
        Cell cell = cellsById.get(rental.cellId());
        if (cell == null) return "—";
        Warehouse warehouse = warehousesById.get(cell.warehouseId());
        return warehouse == null ? "Склад № " + cell.warehouseId() : warehouse.name();
    }

    private String cellNumber(Rental rental) {
        Cell cell = cellsById.get(rental.cellId());
        return cell == null ? "ID " + rental.cellId() : cell.number();
    }

    private CellSize cellSize(Rental rental) {
        Cell cell = cellsById.get(rental.cellId());
        return cell == null ? null : cell.size();
    }
}
