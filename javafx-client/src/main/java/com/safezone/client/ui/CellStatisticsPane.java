package com.safezone.client.ui;

import com.safezone.client.model.Models.CellSize;
import com.safezone.client.model.Models.CellStatistic;
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

/** Shows the rental count returned for each cell. */
public final class CellStatisticsPane extends BorderPane {
    private final SafeZoneClient client;
    private final Window owner;
    private final TableView<CellStatistic> table = new TableView<>();

    public CellStatisticsPane(SafeZoneClient client, Window owner, Runnable onBack) {
        this.client = client;
        this.owner = owner;
        setId("cell-statistics-view");
        setPadding(new Insets(32));
        setTop(Ui.header("Статистика по ячейкам", onBack, this::load));
        configureTable();
        setCenter(table);
        BorderPane.setMargin(table, new Insets(22, 0, 0, 0));
        Platform.runLater(this::load);
    }

    private void configureTable() {
        table.setId("cell-statistics-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setFixedCellSize(46);
        table.setMinHeight(230);
        table.setPlaceholder(Ui.emptyLabel("Нет данных по ячейкам."));
        TableColumn<CellStatistic, String> number = Ui.column("Ячейка", statistic -> statistic.cell().number());
        TableColumn<CellStatistic, CellSize> size = Ui.column("Размер", statistic -> statistic.cell().size());
        TableColumn<CellStatistic, Integer> rentals = Ui.column("Количество аренд", CellStatistic::rentalCount);
        number.setPrefWidth(300);
        size.setPrefWidth(300);
        rentals.setPrefWidth(300);
        table.getColumns().addAll(List.of(number, size, rentals));
    }

    private void load() {
        FxTasks.run(this, client::cellStatistics, statistics -> {
            table.getItems().setAll(statistics);
            table.sort();
        }, error -> Ui.error(owner, error));
    }
}
