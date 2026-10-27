package com.safezone.client.ui.auth;

import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.util.FxTasks;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** Login form with a background request through the shared client. */
public final class LoginView {
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private final SafeZoneClient client;
    private final Runnable onSuccess;
    private final TextField login = new TextField();
    private final PasswordInput password = new PasswordInput("auth-password", "Введите пароль");
    private final Label message = new Label();
    private final Button submit = new Button("Войти");
    private final VBox form;
    private final Parent root;
    private boolean busy;

    public LoginView(SafeZoneClient client, Runnable onSuccess, Runnable onRegister, Runnable onExit) {
        this.client = client;
        this.onSuccess = onSuccess;
        login.setId("auth-login");
        login.setPromptText("Введите логин");
        login.textProperty().addListener((observable, oldValue, newValue) -> login.pseudoClassStateChanged(INVALID, false));
        Label loginLabel = new Label("Логин *");
        loginLabel.getStyleClass().add("field-label");
        loginLabel.setLabelFor(login);
        Label passwordLabel = new Label("Пароль *");
        passwordLabel.getStyleClass().add("field-label");
        message.setId("auth-error");
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        message.setVisible(false);
        message.setManaged(false);

        submit.setId("auth-submit");
        submit.getStyleClass().add("primary-button");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        submit.setOnAction(event -> submit());
        login.setOnAction(event -> password.requestFocus());
        password.setOnAction(this::submit);
        Button register = new Button("Нет аккаунта? Зарегистрироваться");
        register.getStyleClass().add("link-button");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(event -> onRegister.run());
        form = new VBox(16, message, new VBox(7, loginLabel, login),
                new VBox(7, passwordLabel, password.root()), submit, register);
        root = AuthLayout.create(false, form, login::requestFocus, onRegister, onExit);
        Platform.runLater(login::requestFocus);
    }

    public Parent root() { return root; }

    public void showError(String text) { showMessage(text, "error-banner"); }

    public void showInfo(String text) { showMessage(text, "info-banner"); }

    public void setLogin(String value) { login.setText(value); }

    public void clearPassword() { password.clear(); }

    public void setBusy(boolean busy) {
        this.busy = busy;
        form.setDisable(busy);
        root.lookupAll(".auth-tab").forEach(tab -> tab.setDisable(busy));
        submit.setText(busy ? "Входим…" : "Войти");
    }

    private void submit() {
        if (busy) return;
        message.setVisible(false);
        message.setManaged(false);
        boolean missingLogin = login.getText().isBlank();
        boolean missingPassword = password.getText().isEmpty();
        login.pseudoClassStateChanged(INVALID, missingLogin);
        password.setInvalid(missingPassword);
        if (missingLogin || missingPassword) {
            showError("Введите логин и пароль.");
            if (missingLogin) login.requestFocus();
            else password.requestFocus();
            return;
        }
        String suppliedLogin = login.getText();
        String suppliedPassword = password.getText();
        setBusy(true);
        FxTasks.run(root, () -> client.login(suppliedLogin, suppliedPassword), user -> {
            clearPassword();
            setBusy(false);
            onSuccess.run();
        }, error -> {
            clearPassword();
            setBusy(false);
            showError(FxTasks.friendly(error));
        });
    }

    private void showMessage(String text, String styleClass) {
        message.setText(text);
        message.getStyleClass().setAll(styleClass);
        message.setVisible(true);
        message.setManaged(true);
    }
}
