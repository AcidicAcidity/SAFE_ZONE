package com.safezone.client.ui;

import com.safezone.client.model.Models.Cell;
import com.safezone.client.model.Models.CellSize;
import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.util.FxTasks;
import com.safezone.client.util.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

/** Cell selection and hourly duration; creation belongs to SafeZoneClient. */
public final class RentPane extends BorderPane {
    private final SafeZoneClient client;
    private final Runnable onSuccess;
    private final RadioButton specificCell = new RadioButton("Конкретная ячейка");
    private final ComboBox<Cell> cells = new ComboBox<>();
    private final ComboBox<CellSize> sizes = new ComboBox<>();
    private final Spinner<Integer> hours = new Spinner<>(1, 24, 1);
    private final Label error = new Label();

    public RentPane(SafeZoneClient client, Window owner, Runnable onBack, Runnable onSuccess) {
        this.client = client;
        this.onSuccess = onSuccess;
        setId("rent-view");
        setPadding(new Insets(32));
        setTop(Ui.header("Арендовать ячейку", onBack, this::load));
        VBox form = createForm();
        form.setPadding(new Insets(28));
        form.setMaxWidth(700);
        form.setMaxHeight(Region.USE_PREF_SIZE);
        form.getStyleClass().add("surface-card");
        setCenter(form);
        BorderPane.setAlignment(form, Pos.TOP_LEFT);
        BorderPane.setMargin(form, new Insets(22, 0, 0, 0));
        Platform.runLater(this::load);
    }

    private VBox createForm() {
        RadioButton bySize = new RadioButton("По размеру");
        specificCell.setId("rent-mode-cell");
        bySize.setId("rent-mode-size");
        ToggleGroup mode = new ToggleGroup();
        specificCell.setToggleGroup(mode);
        bySize.setToggleGroup(mode);
        specificCell.setSelected(true);

        cells.setId("rent-cell-select");
        cells.setPromptText("Выберите ячейку");
        cells.setMaxWidth(Double.MAX_VALUE);
        cells.setConverter(cellConverter());
        sizes.setId("rent-size-select");
        sizes.getItems().setAll(CellSize.values());
        sizes.setPromptText("Выберите размер");
        sizes.setMaxWidth(Double.MAX_VALUE);
        VBox cellField = field("Ячейка *", cells);
        VBox sizeField = field("Размер *", sizes);
        sizeField.setVisible(false);
        sizeField.setManaged(false);
        mode.selectedToggleProperty().addListener((observable, oldValue, selected) -> {
            if (selected == null) {
                if (oldValue != null) oldValue.setSelected(true);
                return;
            }
            boolean selectCell = selected == specificCell;
            cellField.setVisible(selectCell);
            cellField.setManaged(selectCell);
            sizeField.setVisible(!selectCell);
            sizeField.setManaged(!selectCell);
            showError(null);
        });

        hours.setId("rent-hours");
        hours.setEditable(false);
        hours.setPrefWidth(160);
        Label start = new Label("Начало аренды — при оформлении");
        start.getStyleClass().add("muted");
        error.setId("rent-error");
        error.getStyleClass().add("error-banner");
        error.setWrapText(true);
        error.setMaxWidth(Double.MAX_VALUE);
        showError(null);
        Button submit = new Button("Арендовать");
        submit.setId("rent-submit");
        submit.getStyleClass().add("primary-button");
        submit.setDefaultButton(true);
        submit.setOnAction(event -> submit());
        return new VBox(22, new HBox(24, specificCell, bySize), error, cellField, sizeField,
                field("Продолжительность, часы (1–24) *", hours), start, submit);
    }

    private void load() {
        FxTasks.run(this, client::availableCells, available -> {
            cells.getItems().setAll(available);
            cells.setPromptText(available.isEmpty() ? "Свободных ячеек нет" : "Выберите ячейку");
        }, failure -> showError(FxTasks.friendly(failure)));
    }

    private void submit() {
        boolean selectCell = specificCell.isSelected();
        Cell selectedCell = cells.getValue();
        CellSize selectedSize = sizes.getValue();
        int duration = hours.getValue();
        if (selectCell && selectedCell == null) {
            showError("Выберите ячейку для аренды.");
            cells.requestFocus();
            return;
        }
        if (!selectCell && selectedSize == null) {
            showError("Выберите размер ячейки.");
            sizes.requestFocus();
            return;
        }
        showError(null);
        FxTasks.mutate(this, () -> selectCell
                ? client.rentSpecificCell(selectedCell.id(), duration)
                : client.rentCellBySize(selectedSize, duration),
                rental -> onSuccess.run(), failure -> showError(FxTasks.friendly(failure)));
    }

    private void showError(String message) {
        boolean visible = message != null && !message.isBlank();
        error.setText(visible ? message : "");
        error.setVisible(visible);
        error.setManaged(visible);
    }

    private static VBox field(String text, Node input) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return new VBox(9, label, input);
    }

    private static StringConverter<Cell> cellConverter() {
        return new StringConverter<>() {
            @Override public String toString(Cell cell) {
                return cell == null ? "" : cell.number() + " · " + cell.size()
                        + " · Склад № " + cell.warehouseId() + " · ID " + cell.id();
            }
            @Override public Cell fromString(String text) { throw new UnsupportedOperationException(); }
        };
    }
}
