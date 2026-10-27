package com.safezone.client.util;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/** Small shared controls and dialogs; individual panes own their workflows. */
public final class Ui {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Ui() { }

    public static void error(Window owner, Throwable error) {
        dialog(owner, Alert.AlertType.ERROR, "Операция не выполнена", FxTasks.friendly(error)).showAndWait();
    }

    public static void info(Window owner, String message) {
        dialog(owner, Alert.AlertType.INFORMATION, null, message).showAndWait();
    }

    public static boolean confirm(Window owner, String message) {
        Alert alert = dialog(owner, Alert.AlertType.CONFIRMATION, "Подтверждение", message);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private static Alert dialog(Window owner, Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(owner);
        alert.setTitle("Аренда ячеек");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().setPrefWidth(490);
        if (owner != null && owner.getScene() != null) {
            alert.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }
        return alert;
    }

    public static HBox header(String title, Runnable onBack, Runnable onRefresh) {
        Label heading = new Label(title);
        heading.getStyleClass().add("page-title");
        heading.setWrapText(true);
        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        HBox header = new HBox(12, heading, space);
        header.setAlignment(Pos.CENTER_LEFT);
        if (onBack != null) header.getChildren().add(button("Назад", "page-back", onBack));
        if (onRefresh != null) header.getChildren().add(button("Обновить", "page-refresh", onRefresh));
        return header;
    }

    private static Button button(String title, String id, Runnable action) {
        Button button = new Button(title);
        button.setId(id);
        button.getStyleClass().add("secondary-button");
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setOnAction(event -> action.run());
        return button;
    }

    public static <T, V> TableColumn<T, V> column(String title, Function<T, V> value) {
        TableColumn<T, V> column = new TableColumn<>(title);
        column.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(value.apply(row.getValue())));
        column.setPrefWidth(150);
        return column;
    }

    public static Label emptyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted");
        return label;
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "—" : DATE_TIME.format(value);
    }

    public static Button card(String id, String title, String description, Runnable action) {
        Label name = new Label(title);
        name.getStyleClass().add("section-title");
        name.setWrapText(true);
        Label details = emptyLabel(description);
        details.setWrapText(true);
        VBox text = new VBox(10, name, details);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label arrow = new Label("›");
        arrow.getStyleClass().add("navigation-arrow");
        HBox content = new HBox(18, text, arrow);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setMouseTransparent(true);
        Button card = new Button();
        card.setId(id);
        card.getStyleClass().add("navigation-card");
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.CENTER_LEFT);
        content.prefWidthProperty().bind(Bindings.max(0, card.widthProperty().subtract(54)));
        card.setGraphic(content);
        card.setAccessibleText(title + ". " + description);
        card.setOnAction(event -> action.run());
        return card;
    }
}
