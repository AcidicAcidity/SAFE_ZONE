package com.safeZone.api;


import com.safeZone.util.DBHelper;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class ApiServer {
    public static void start(DBHelper db) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/user",    new UserApi(db));
        server.createContext("/api/bin",     new BinApi(db));
        server.createContext("/api/payment", new PaymentApi(db));
        server.setExecutor(null);
        server.start();
        System.out.println("API: http://localhost:8080");
    }
}
