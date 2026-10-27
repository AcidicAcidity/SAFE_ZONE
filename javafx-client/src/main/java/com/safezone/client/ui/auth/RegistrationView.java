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

import java.util.function.Consumer;

/** Registration form with local field checks and a background client request. */
public final class RegistrationView {
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private final SafeZoneClient client;
    private final Consumer<String> onRegistered;
    private final TextField login = new TextField();
    private final PasswordInput password = new PasswordInput("auth-password", "Придумайте пароль");
    private final PasswordInput confirmation = new PasswordInput("auth-confirm-password", "Повторите пароль");
    private final Label message = new Label();
    private final Button submit = new Button("Зарегистрироваться");
    private final VBox form;
    private final Parent root;
    private boolean busy;

    public RegistrationView(SafeZoneClient client, Consumer<String> onRegistered, Runnable onLogin, Runnable onExit) {
        this.client = client;
        this.onRegistered = onRegistered;
        login.setId("auth-login");
        login.setPromptText("Введите логин");
        login.textProperty().addListener((observable, oldValue, newValue) -> login.pseudoClassStateChanged(INVALID, false));
        Label loginLabel = new Label("Логин *");
        loginLabel.getStyleClass().add("field-label");
        loginLabel.setLabelFor(login);
        Label loginHint = new Label("Не менее 3 символов");
        loginHint.getStyleClass().add("small-text");
        Label passwordLabel = new Label("Пароль *");
        passwordLabel.getStyleClass().add("field-label");
        Label passwordHint = new Label("Не менее 4 символов");
        passwordHint.getStyleClass().add("small-text");
        Label confirmationLabel = new Label("Повторите пароль *");
        confirmationLabel.getStyleClass().add("field-label");
        message.setId("auth-error");
        message.getStyleClass().add("error-banner");
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
        password.setOnAction(confirmation::requestFocus);
        confirmation.setOnAction(this::submit);
        Button back = new Button("Уже есть аккаунт? Войти");
        back.getStyleClass().add("link-button");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(event -> onLogin.run());
        form = new VBox(16, message, new VBox(7, loginLabel, login, loginHint),
                new VBox(7, passwordLabel, password.root(), passwordHint),
                new VBox(7, confirmationLabel, confirmation.root()), submit, back);
        root = AuthLayout.create(true, form, onLogin, login::requestFocus, onExit);
        Platform.runLater(login::requestFocus);
    }

    public Parent root() { return root; }

    public void showError(String text) {
        message.setText(text);
        message.setVisible(true);
        message.setManaged(true);
    }

    public void clearPasswords() {
        password.clear();
        confirmation.clear();
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
        form.setDisable(busy);
        root.lookupAll(".auth-tab").forEach(tab -> tab.setDisable(busy));
        submit.setText(busy ? "Регистрируем…" : "Зарегистрироваться");
    }

    private void submit() {
        if (busy) return;
        String loginValue = login.getText();
        String passwordValue = password.getText();
        boolean invalidLogin = loginValue.isBlank() || loginValue.length() < 3;
        boolean invalidPassword = passwordValue.length() < 4;
        boolean mismatch = !passwordValue.equals(confirmation.getText());
        login.pseudoClassStateChanged(INVALID, invalidLogin);
        password.setInvalid(invalidPassword);
        confirmation.setInvalid(mismatch);
        if (invalidLogin || invalidPassword || mismatch) {
            showError(invalidLogin ? "Логин должен содержать не менее 3 символов."
                    : invalidPassword ? "Пароль должен содержать не менее 4 символов."
                    : "Пароли не совпадают.");
            if (invalidLogin) login.requestFocus();
            else if (invalidPassword) password.requestFocus();
            else confirmation.requestFocus();
            return;
        }
        message.setVisible(false);
        message.setManaged(false);
        setBusy(true);
        FxTasks.run(root, () -> client.register(loginValue, passwordValue), user -> {
            clearPasswords();
            setBusy(false);
            onRegistered.accept(user.login());
        }, error -> {
            setBusy(false);
            showError(FxTasks.friendly(error));
        });
    }
}
