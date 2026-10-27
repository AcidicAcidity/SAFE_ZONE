package com.safezone.client;

import com.safezone.client.net.SafeZoneClient;
import com.safezone.client.ui.MainView;
import com.safezone.client.ui.auth.LoginView;
import com.safezone.client.ui.auth.RegistrationView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

/** Owns the shared client and switches between authentication and the workspace. */
public final class SafeZoneApplication extends Application {
    private Stage stage;
    private Scene scene;
    private SafeZoneClient client;
    private LoginView loginView;
    private RegistrationView registrationView;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        client = new SafeZoneClient();
        scene = new Scene(new StackPane(), 1200, 850);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/com/safezone/client/styles.css")).toExternalForm());

        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.setOnCloseRequest(event -> client.logout());
        showLogin();
        stage.show();
    }

    private void showLogin() {
        clearPasswords();
        registrationView = null;
        loginView = new LoginView(client, this::showMain, this::showRegistration, Platform::exit);
        scene.setRoot(loginView.root());
        stage.setTitle("SafeZone — Вход");
    }

    private void showRegistration() {
        clearPasswords();
        loginView = null;
        registrationView = new RegistrationView(client, login -> {
            showLogin();
            loginView.setLogin(login);
            loginView.showInfo("Регистрация завершена. Войдите в аккаунт.");
        }, this::showLogin, Platform::exit);
        scene.setRoot(registrationView.root());
        stage.setTitle("SafeZone — Регистрация");
    }

    private void showMain() {
        clearPasswords();
        loginView = null;
        registrationView = null;
        scene.setRoot(new MainView(client, stage, this::logoutAndShowLogin));
        stage.setTitle("SafeZone — Главная");
    }

    private void logoutAndShowLogin() {
        client.logout();
        showLogin();
    }

    private void clearPasswords() {
        if (loginView != null) loginView.clearPassword();
        if (registrationView != null) registrationView.clearPasswords();
    }

    @Override
    public void stop() {
        clearPasswords();
        if (client != null) client.logout();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
