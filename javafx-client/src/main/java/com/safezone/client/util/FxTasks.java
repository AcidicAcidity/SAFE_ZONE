package com.safezone.client.util;

import com.safezone.client.net.ClientException;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Runs client calls outside the JavaFX thread, as in the supplied example. */
public final class FxTasks {
    private FxTasks() { }

    public static <T> Task<T> run(Node view, Callable<T> work,
                                  Consumer<T> success, Consumer<Throwable> failure) {
        return start(view, false, work, success, failure);
    }

    public static <T> Task<T> mutate(Node view, Callable<T> work,
                                     Consumer<T> success, Consumer<Throwable> failure) {
        return start(view, true, work, success, failure);
    }

    private static <T> Task<T> start(Node view, boolean mutation, Callable<T> work,
                                    Consumer<T> success, Consumer<Throwable> failure) {
        Scene scene = view.getScene();
        if (scene == null || view.isDisabled()) return null;
        Window owner = scene.getWindow();
        Node blocked = mutation ? scene.getRoot() : view;
        blocked.setDisable(true);

        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return work.call(); }
        };
        ChangeListener<Scene> detach = (observable, previous, current) -> {
            if (current != scene && !mutation) task.cancel(true);
        };
        view.sceneProperty().addListener(detach);
        ChangeListener<Boolean> closing = (observable, previous, showing) -> {
            if (!showing) task.cancel(true);
        };
        if (owner != null) owner.showingProperty().addListener(closing);
        Runnable finish = () -> {
            blocked.setDisable(false);
            view.sceneProperty().removeListener(detach);
            if (owner != null) owner.showingProperty().removeListener(closing);
        };
        task.setOnSucceeded(event -> {
            finish.run();
            if (view.getScene() == scene && owner != null && owner.isShowing()) success.accept(task.getValue());
        });
        task.setOnFailed(event -> {
            finish.run();
            if (view.getScene() == scene && owner != null && owner.isShowing()) failure.accept(task.getException());
        });
        task.setOnCancelled(event -> finish.run());
        Thread worker = new Thread(task, "safezone-client");
        worker.setDaemon(true);
        worker.start();
        return task;
    }

    public static String friendly(Throwable error) {
        return error instanceof ClientException ? error.getMessage()
                : "Не удалось выполнить действие. Повторите попытку.";
    }
}
