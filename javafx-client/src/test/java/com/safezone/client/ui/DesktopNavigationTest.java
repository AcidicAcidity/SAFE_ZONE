package com.safezone.client.ui;

import com.safezone.client.SafeZoneApplication;
import com.safezone.client.model.Models.RentalStatus;
import com.safezone.client.model.Models.UserStatus;
import com.safezone.client.model.Models.Cell;
import com.safezone.client.model.Models.Rental;
import com.safezone.client.model.Models.User;
import com.safezone.client.net.SafeZoneClient;
import com.safezone.infrastructure.data.DBHelper;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperties;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Explicitly enabled integration scenario against the configured database and display. */
@EnabledIfSystemProperties({
        @EnabledIfSystemProperty(named = "safezone.uiTests", matches = "true"),
        @EnabledIfSystemProperty(named = "safezone.dbTests", matches = "true")
})
final class DesktopNavigationTest {
    private Stage stage;
    private SafeZoneApplication application;
    private final String temporaryLogin = "fxcheck_" + System.currentTimeMillis();

    @Test
    @Timeout(60)
    void realRegistrationRentalAndAccountManagementWork() throws Exception {
        DBHelper database = new DBHelper();
        assertNull(database.getSingleRow("SELECT user_id FROM users WHERE login = ?", temporaryLogin));
        Path export = Path.of("target", "integration-export-" + temporaryLogin + ".xlsx");
        SafeZoneClient verificationClient = new SafeZoneClient();
        try {
            verificationClient.login("root", "root");
            verificationClient.exportDatabase(export);
            try (var input = Files.newInputStream(export); var workbook = new XSSFWorkbook(input)) {
                for (String sheetName : Set.of("users", "warehouses", "cells", "rentals")) {
                    var sheet = workbook.getSheet(sheetName);
                    assertNotNull(sheet, "Missing exported table: " + sheetName);
                    assertTrue(sheet.getPhysicalNumberOfRows() > 0, "Exported table has no header: " + sheetName);
                }
            }
        } finally {
            verificationClient.logout();
            Files.deleteIfExists(export);
        }
        startToolkit();
        AtomicBoolean exitRequested = new AtomicBoolean();
        try {
            fx(() -> {
                Platform.setImplicitExit(false);
                application = new SafeZoneApplication();
                stage = new Stage();
                application.start(stage);
                return null;
            });
            await("login form", () -> exists("auth-submit") && !button("auth-submit").isDisabled());
            fx(() -> { assertNoPreviewText(); return null; });
            screenshot("login.png");
            login("root", "root");
            fx(() -> {
                assertEquals(Set.of("menu-warehouses", "menu-cells", "menu-rent", "menu-my-rentals", "menu-admin"), cardIds());
                assertNoPreviewText();
                return null;
            });
            screenshot("main.png");

            open("menu-warehouses", "warehouses-table");
            assertEquals(rowCount(database, "warehouses"), fx(() -> table("warehouses-table").getItems().size()));
            back("main-menu-view");
            open("menu-cells", "cells-table");
            assertEquals(rowCount(database, "cells"), fx(() -> table("cells-table").getItems().size()));
            screenshot("cells.png");
            back("main-menu-view");
            open("menu-admin", "admin-menu-view");
            open("admin-statistics", "cell-statistics-table");
            assertEquals(rowCount(database, "cells"), fx(() -> table("cell-statistics-table").getItems().size()));
            back("admin-menu-view");
            open("admin-users", "users-table");
            assertEquals(rowCount(database, "users"), fx(() -> table("users-table").getItems().size()));
            screenshot("users.png");
            back("admin-menu-view");
            open("admin-rentals", "rentals-table");
            assertEquals(rowCount(database, "rentals"), fx(() -> table("rentals-table").getItems().size()));
            logout();

            fx(() -> { node("auth-tab-register", ToggleButton.class).fire(); return null; });
            await("registration form", () -> exists("auth-confirm-password"));
            screenshot("registration.png");
            fx(() -> {
                text("auth-login").setText(temporaryLogin);
                text("auth-password").setText("check1234");
                text("auth-confirm-password").setText("different");
                button("auth-submit").fire();
                assertEquals("Пароли не совпадают.", label("auth-error").getText());
                text("auth-confirm-password").setText("check1234");
                button("auth-submit").fire();
                return null;
            });
            await("registration completed", () -> !exists("auth-confirm-password") && exists("auth-error")
                    && label("auth-error").getText().contains("Регистрация завершена"));
            Map<String, Object> account = database.getSingleRow("SELECT user_id FROM users WHERE login = ?", temporaryLogin);
            assertNotNull(account, "UI registration must create an actual user");
            int temporaryUserId = ((Number) account.get("user_id")).intValue();
            login(temporaryLogin, "check1234");
            fx(() -> { assertFalse(exists("menu-admin")); return null; });
            open("menu-rent", "rent-cell-select");
            int selectedCellId = fx(() -> {
                ComboBox<Cell> cells = cellChoices();
                assertFalse(cells.getItems().isEmpty(), "The integration database needs an available cell");
                cells.getSelectionModel().selectFirst();
                hours().getValueFactory().setValue(1);
                int cellId = cells.getValue().id();
                button("rent-submit").fire();
                return cellId;
            });
            await("new rental in my rentals", () -> exists("rentals-table") && table("rentals-table").getItems().size() == 1);
            int createdRentalId = fx(() -> {
                Rental rental = (Rental) table("rentals-table").getItems().getFirst();
                assertEquals(temporaryUserId, rental.userId());
                assertEquals(selectedCellId, rental.cellId());
                assertEquals(Duration.ofHours(1), Duration.between(rental.startDateTime(), rental.endDateTime()));
                return rental.id();
            });
            assertNotNull(database.getSingleRow("SELECT rental_id FROM rentals WHERE rental_id = ? AND user_id = ?", createdRentalId, temporaryUserId));
            logout();
            login("root", "root");
            open("menu-admin", "admin-menu-view");
            open("admin-rentals", "rentals-table");
            await("own rental in administrator table", () -> rentals().getItems().stream()
                    .anyMatch(rental -> rental.id() == createdRentalId && rental.userId() == temporaryUserId));
            fx(() -> {
                Rental owned = rentals().getItems().stream().filter(rental -> rental.id() == createdRentalId
                        && rental.userId() == temporaryUserId).findFirst().orElseThrow();
                assertEquals(RentalStatus.ACTIVE, owned.status());
                rentals().getSelectionModel().select(owned);
                assertFalse(button("rental-cancel").isDisabled());
                Platform.runLater(() -> button("rental-cancel").fire());
                return null;
            });
            await("own rental cancellation confirmation", () -> confirmationDialog() != null);
            fx(() -> { ((Button) confirmationDialog().lookupButton(ButtonType.OK)).fire(); return null; });
            await("own rental cancelled", () -> exists("rentals-table") && !rentals().isDisabled()
                    && rentals().getItems().stream().anyMatch(rental -> rental.id() == createdRentalId
                    && rental.userId() == temporaryUserId && rental.status() == RentalStatus.CANCELLED));
            assertEquals("CANCELLED", database.getSingleRow("SELECT status FROM rentals WHERE rental_id = ? AND user_id = ?",
                    createdRentalId, temporaryUserId).get("status"));
            back("admin-menu-view");
            open("admin-users", "users-table");
            changeOwnUserStatus("user-block", UserStatus.BLOCKED, temporaryUserId, false);
            changeOwnUserStatus("user-unblock", UserStatus.ACTIVE, temporaryUserId, false);
            changeOwnUserStatus("user-delete", UserStatus.DELETED, temporaryUserId, true);
            assertEquals("DELETED", database.getSingleRow("SELECT status FROM users WHERE user_id = ? AND login = ?", temporaryUserId, temporaryLogin).get("status"));
            logout();
            fx(() -> {
                assertTrue(text("auth-login").getText().isEmpty());
                assertTrue(text("auth-password").getText().isEmpty());
                assertNoPreviewText();
                return null;
            });
            cleanupOwnRows(database);

            CountDownLatch closed = new CountDownLatch(1);
            fx(() -> {
                stage.showingProperty().addListener((observable, oldValue, showing) -> { if (!showing) closed.countDown(); });
                Button exit = button("app-exit");
                exitRequested.set(true);
                exit.fire();
                return null;
            });
            assertTrue(closed.await(10, TimeUnit.SECONDS), "Exit action did not close the application window");
        } finally {
            try {
                cleanupOwnRows(database);
            } finally {
                if (!exitRequested.get()) {
                    fx(() -> {
                        if (application != null) application.stop();
                        if (stage != null) stage.close();
                        return null;
                    });
                    Platform.exit();
                }
            }
        }
    }

    private void changeOwnUserStatus(String action, UserStatus expected, int userId, boolean confirmation) throws Exception {
        fx(() -> {
            User owned = users().getItems().stream().filter(user -> user.id() == userId && temporaryLogin.equals(user.login())).findFirst().orElseThrow();
            users().getSelectionModel().select(owned);
            assertFalse(button(action).isDisabled());
            if (confirmation) Platform.runLater(() -> button(action).fire());
            else button(action).fire();
            return null;
        });
        if (confirmation) {
            await("own account deletion confirmation", () -> confirmationDialog() != null);
            fx(() -> { ((Button) confirmationDialog().lookupButton(ButtonType.OK)).fire(); return null; });
        }
        await("updated own account status " + expected, () -> exists("users-table") && !users().isDisabled()
                && users().getItems().stream().anyMatch(user -> user.id() == userId && user.status() == expected));
    }

    private DialogPane confirmationDialog() {
        return Window.getWindows().stream().filter(window -> window != stage && window.isShowing() && window.getScene() != null)
                .map(window -> window.getScene().getRoot()).filter(DialogPane.class::isInstance).map(DialogPane.class::cast)
                .filter(pane -> pane.getButtonTypes().contains(ButtonType.OK)).findFirst().orElse(null);
    }

    private void cleanupOwnRows(DBHelper database) throws SQLException {
        Map<String, Object> account = database.getSingleRow("SELECT user_id FROM users WHERE login = ?", temporaryLogin);
        if (account == null) return;
        int userId = ((Number) account.get("user_id")).intValue();
        database.executeUpdateData("DELETE FROM rentals WHERE user_id = ?", userId);
        database.executeUpdateData("DELETE FROM users WHERE user_id = ? AND login = ?", userId, temporaryLogin);
    }

    private static int rowCount(DBHelper database, String table) throws SQLException {
        String sql = switch (table) {
            case "warehouses" -> "SELECT COUNT(*) AS count FROM warehouses";
            case "cells" -> "SELECT COUNT(*) AS count FROM cells";
            case "users" -> "SELECT COUNT(*) AS count FROM users";
            case "rentals" -> "SELECT COUNT(*) AS count FROM rentals";
            default -> throw new IllegalArgumentException(table);
        };
        return ((Number) database.getSingleRow(sql).get("count")).intValue();
    }

    private void login(String login, String password) throws Exception {
        fx(() -> {
            text("auth-login").setText(login);
            text("auth-password").setText(password);
            assertFalse(button("auth-submit").isDisabled());
            button("auth-submit").fire();
            return null;
        });
        await("login " + login, () -> exists("main-menu-view") && !node("main-menu-view", Parent.class).isDisabled());
    }

    private void open(String action, String destination) throws Exception {
        fx(() -> { button(action).fire(); return null; });
        await(destination, () -> exists(destination) && !root().lookup("#" + destination).isDisabled());
    }

    private void back(String destination) throws Exception { open("page-back", destination); }

    private void logout() throws Exception {
        fx(() -> {
            node("account-menu", MenuButton.class).getItems().stream().filter(item -> "account-logout".equals(item.getId())).findFirst().orElseThrow().fire();
            return null;
        });
        await("logout", () -> exists("auth-submit") && !button("auth-submit").isDisabled());
    }

    private void assertNoPreviewText() {
        assertFalse(exists("auth-demo"));
        String visibleCopy = root().lookupAll(".label").stream().filter(Node::isVisible).map(node -> ((Label) node).getText()).collect(Collectors.joining(" ")).toLowerCase();
        assertFalse(visibleCopy.contains("backend"));
        assertFalse(visibleCopy.contains("предпросмотр"));
        assertFalse(visibleCopy.contains("тестовый вход"));
        assertFalse(visibleCopy.contains("демонстрацион"));
    }

    private Set<String> cardIds() { return root().lookupAll(".navigation-card").stream().map(Node::getId).collect(Collectors.toSet()); }
    private Parent root() { return stage.getScene().getRoot(); }
    private boolean exists(String id) { return root().lookup("#" + id) != null; }
    private TextField text(String id) { return node(id, TextField.class); }
    private Button button(String id) { return node(id, Button.class); }
    private Label label(String id) { return node(id, Label.class); }
    private TableView<?> table(String id) { return node(id, TableView.class); }
    @SuppressWarnings("unchecked") private TableView<Rental> rentals() { return node("rentals-table", TableView.class); }
    @SuppressWarnings("unchecked") private TableView<User> users() { return node("users-table", TableView.class); }
    @SuppressWarnings("unchecked") private ComboBox<Cell> cellChoices() { return node("rent-cell-select", ComboBox.class); }
    @SuppressWarnings("unchecked") private Spinner<Integer> hours() { return node("rent-hours", Spinner.class); }

    private <T extends Node> T node(String id, Class<T> type) {
        Node node = root().lookup("#" + id);
        assertNotNull(node, "Missing UI element: " + id);
        return type.cast(node);
    }

    private static void startToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try { Platform.startup(started::countDown); }
        catch (IllegalStateException alreadyStarted) { Platform.runLater(started::countDown); }
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX toolkit did not start");
    }

    private static <T> T fx(Callable<T> work) throws Exception {
        if (Platform.isFxApplicationThread()) return work.call();
        FutureTask<T> task = new FutureTask<>(work);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }

    private void await(String description, BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (System.nanoTime() < deadline) {
            if (fx(condition::getAsBoolean)) return;
            Thread.sleep(25);
        }
        fail("Timed out waiting for " + description);
    }

    private void screenshot(String name) throws Exception {
        BufferedImage image = fx(() -> {
            Parent root = root();
            root.applyCss();
            root.layout();
            WritableImage snapshot = root.snapshot(new SnapshotParameters(), null);
            int width = (int) snapshot.getWidth(), height = (int) snapshot.getHeight();
            int[] pixels = new int[width * height];
            snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), pixels, 0, width);
            BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            buffered.setRGB(0, 0, width, height, pixels, 0, width);
            return buffered;
        });
        Path directory = Path.of("target", "screenshots");
        Files.createDirectories(directory);
        assertTrue(ImageIO.write(image, "png", directory.resolve(name).toFile()));
    }
}
