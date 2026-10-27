package com.safezone.client;

/** Separate launcher supports the packaged application's classpath runtime. */
public final class Launcher {
    private Launcher() { }

    public static void main(String[] args) {
        SafeZoneApplication.main(args);
    }
}
