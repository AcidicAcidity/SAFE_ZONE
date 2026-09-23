package com.safeZone.views;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.safeZone.util.*;

public class AuthMenu {
    private static final Logger log = LoggerFactory.getLogger(AuthMenu.class);
    private final DBHelper dbHelper;
    private final SafeZoneService service;
    private final AppMenus appMenus;
    private WindowBasedTextGUI gui;

    public AuthMenu(DBHelper dbHelper, SafeZoneService service, AppMenus appMenus) {
        this.dbHelper = dbHelper;
        this.service = service;
        this.appMenus = appMenus;
    }

    public void start() throws Exception {
        Screen screen = new DefaultTerminalFactory()
                .setTerminalEmulatorTitle("SafeZone")
                .createScreen();
        screen.startScreen();

        gui = new MultiWindowTextGUI(
            screen,
            new DefaultWindowManager(),
            new EmptySpace(TextColor.ANSI.BLACK)
        );
        gui.setTheme(ThemeGUI.build());

        showAuthMenu();

        screen.stopScreen();
    }

    private void showAuthMenu() {
        BasicWindow mainWindow = new BasicWindow("Страница авторизации");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button signIn = new Button("Войти", () -> {
            mainWindow.close();
            showSignInMenu();
        });
        Button signUp = new Button("Зарегистрироваться", () -> {
            mainWindow.close();
            showSignUpMenu();
        });
        Button exit = new Button("Выход", () -> {
            mainWindow.close();
            System.exit(0);
        });

        panel.addComponent(new Label("SAFE ZONE"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(signIn);
        panel.addComponent(signUp);
        panel.addComponent(new EmptySpace());
        panel.addComponent(exit);

        mainWindow.setComponent(panel);
        gui.addWindowAndWait(mainWindow);
    }

    private void showSignInMenu() {
        BasicWindow signInWindow = new BasicWindow("Вход");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        TextBox usernameBox = new TextBox("Имя пользователя");
        TextBox passwordBox = new TextBox("Пароль");
        Button signInButton = new Button("Войти", () -> {
            String username = usernameBox.getText();
            String password = passwordBox.getText();

            if (username.isEmpty() || password.isEmpty()) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Username и Password не могут быть пустыми");
                return;
            }

            boolean isAuth;
            try {
                isAuth = service.authUser(username, password) != null;
            } catch (SQLException e) {
                log.error("Failed to authenticate", e);
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось выполнить вход");
                return;
            }

            if (isAuth) {
                log.info("Authentication successful");
                signInWindow.close();
                appMenus.start(gui);
            } else {
                log.error("Invalid username or password");
                MessageDialog.showMessageDialog(gui, "Error", "Invalid username or password");
            }
        });
        Button signUpButton = new Button("Зарегистрироваться", () -> {
            signInWindow.close();
            showSignUpMenu();
        });
        Button exit = new Button("Выход", () -> {
            signInWindow.close();
            showAuthMenu();
        });
        panel.addComponent(new Label("SAFE_ZONE"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("АВТОРИЗАЦИЯ"));
        panel.addComponent(usernameBox);
        panel.addComponent(passwordBox);
        panel.addComponent(signInButton);
        panel.addComponent(new EmptySpace());
        panel.addComponent(signUpButton);
        panel.addComponent(exit);
        signInWindow.setComponent(panel);
        gui.addWindowAndWait(signInWindow);
    }

    private void showSignUpMenu() {
        BasicWindow signUpWindow = new BasicWindow("Регистрация");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        TextBox usernameBox = new TextBox("Имя пользователя");
        TextBox passwordBox = new TextBox("Пароль");
        Button signUpButton = new Button("Зарегистрироваться", () -> {
            String username = usernameBox.getText();
            String password = passwordBox.getText();

            if (username.isEmpty() || password.isEmpty()) {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Username и Password не могут быть пустыми");
                return;
            }

            try {
                service.createUser(username, password);
            } catch (SQLException e) {
                log.error("Failed to add user", e);
                MessageDialog.showMessageDialog(gui, "Ошибка", "Не удалось зарегистрировать пользователя");
                return;
            }

            signUpWindow.close();
            appMenus.start(gui);
        });
        Button exit = new Button("Выход", () -> {
            signUpWindow.close();
            showAuthMenu();
        });
        panel.addComponent(new Label("SAFE_ZONE"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Регистрация"));
        panel.addComponent(usernameBox);
        panel.addComponent(passwordBox);
        panel.addComponent(signUpButton);
        panel.addComponent(exit);
        signUpWindow.setComponent(panel);
        gui.addWindowAndWait(signUpWindow);
    }
}
