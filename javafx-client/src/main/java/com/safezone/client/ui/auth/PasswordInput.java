package com.safezone.client.ui.auth;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/** A password field with an explicit visibility switch. */
public final class PasswordInput {
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private final PasswordField hidden = new PasswordField();
    private final TextField revealed = new TextField();
    private final ToggleButton visibility = new ToggleButton("Показать");
    private final HBox root;

    public PasswordInput(String id, String prompt) {
        hidden.setId(id);
        hidden.setPromptText(prompt);
        revealed.setId(id + "-visible");
        revealed.textProperty().bindBidirectional(hidden.textProperty());
        revealed.promptTextProperty().bind(hidden.promptTextProperty());
        revealed.setVisible(false);
        revealed.setManaged(false);
        StackPane fields = new StackPane(hidden, revealed);
        fields.setMinWidth(0);
        HBox.setHgrow(fields, Priority.ALWAYS);
        visibility.setId(id + "-toggle");
        visibility.getStyleClass().add("password-toggle");
        visibility.setAccessibleText("Показать пароль");
        visibility.setOnAction(event -> updateVisibility());
        root = new HBox(8, fields, visibility);
        root.setAlignment(Pos.CENTER_LEFT);
        hidden.textProperty().addListener((observable, oldValue, newValue) -> setInvalid(false));
    }

    public Parent root() { return root; }

    public String getText() { return hidden.getText(); }

    public void setText(String value) { hidden.setText(value); }

    public void clear() {
        hidden.clear();
        visibility.setSelected(false);
        updateVisibility();
    }

    public void requestFocus() {
        (visibility.isSelected() ? revealed : hidden).requestFocus();
    }

    public void setOnAction(Runnable action) {
        hidden.setOnAction(event -> action.run());
        revealed.setOnAction(event -> action.run());
    }

    public void setInvalid(boolean invalid) {
        hidden.pseudoClassStateChanged(INVALID, invalid);
        revealed.pseudoClassStateChanged(INVALID, invalid);
    }

    private void updateVisibility() {
        boolean show = visibility.isSelected();
        hidden.setVisible(!show);
        hidden.setManaged(!show);
        revealed.setVisible(show);
        revealed.setManaged(show);
        visibility.setText(show ? "Скрыть" : "Показать");
        visibility.setAccessibleText(show ? "Скрыть пароль" : "Показать пароль");
    }
}
