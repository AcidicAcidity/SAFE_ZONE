package com.safeZone.views;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.slf4j.*;
import com.safeZone.util.DBHelper;

public class AuthMenu {
    private static final Logger log = LoggerFactory.getLogger(AuthMenu.class);
    private DBHelper dbHelper;
    private AppMenus appMenus;
    private static WindowBasedTextGUI gui;

    public AuthMenu(DBHelper dbHelper){
        this.dbHelper = dbHelper;
    }

    public AuthMenu(AppMenus appMenus) {
        this.appMenus = appMenus;
    }

    public void start() throws Exception {
        Screen screen = new DefaultTerminalFactory().createScreen();
        screen.startScreen();

        gui = new MultiWindowTextGUI(screen);

        showAuthMenu();

        screen.stopScreen();
    }

    private void showAuthMenu() {
        BasicWindow mainWindow = new BasicWindow("Страница авторизации");
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        Button signIn = new Button("Войти", () -> {
            showSignInMenu();
        });
        Button signUp = new Button("Зарегистрироваться", () -> {
            showSignUpMenu();
        });
        Button exit = new Button("Выход", () -> {
            mainWindow.close();
            System.exit(0);
        });

        panel.addComponent(new Label("SAFE ZONE"));
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

            if ((username.isEmpty() == false) && (password.isEmpty() == false)) {
                if (dbHelper.userAuth(username, password) == true) {
                    log.info("Authentication successful");
                    signInWindow.close();
                    try {
                        appMenus.start();
                    } catch (Exception e) {
                        log.error("Failed to start app menus", e);
                    }
                } else {
                    log.error("Invalid username or password");
                    MessageDialog.showMessageDialog(gui, "Error", "Invalid username or password");
                }
            } else {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Username и Password не могут быть пустыми");
            }
        });
        Button signUpButton = new Button("Зарегистрироваться", () -> {
            signInWindow.close();
            showSignUpMenu();
        });
        panel.addComponent(new Label("SAFE_ZONE"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("АВТОРИЗАЦИЯ"));
        panel.addComponent(usernameBox);
        panel.addComponent(passwordBox);
        panel.addComponent(signInButton);
        panel.addComponent(new EmptySpace());
        panel.addComponent(signUpButton);
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
            if ((username.isEmpty() == false) && (password.isEmpty() == false)) {
                dbHelper.addUser(username, password);
                signUpWindow.close();
                try {
                    appMenus.start();
                } catch (Exception e) {
                    log.error("Failed to start app menus", e);
                }
            } else {
                MessageDialog.showMessageDialog(gui, "Ошибка", "Username и Password не могут быть пустыми");
            }
        });
        panel.addComponent(new Label("SAFE_ZONE"));
        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Регистрация"));
        panel.addComponent(usernameBox);
        panel.addComponent(passwordBox);
        panel.addComponent(signUpButton);
        signUpWindow.setComponent(panel);
        gui.addWindowAndWait(signUpWindow);
    }
}
