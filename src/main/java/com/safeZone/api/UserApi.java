package com.safeZone.api;

import com.safeZone.model.User;
import com.safeZone.util.DBHelper;
import com.safeZone.util.JsonUtils;
import com.safeZone.util.SafeZoneService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserApi implements HttpHandler {

    private final SafeZoneService service;

    public UserApi(DBHelper db) {
        this.service = new SafeZoneService(db);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String query  = ex.getRequestURI().getQuery();
        String response = "";
        int status = 200;

        try {
            switch (method) {

                // ============ POST ============
                case "POST" -> {
                    Map<String, Object> b = readJson(ex);

                    if (path.endsWith("/auth")) {
                        // POST /api/user/auth
                        User user = service.authUser((String) b.get("login"),
                                                     (String) b.get("password"));
                        if (user == null) {
                            status = 401;
                            response = "{\"ok\":false}";
                        } else {
                            response = JsonUtils.toJson(user);
                        }
                    } else {
                        // POST /api/user
                        boolean ok = service.createUser((String) b.get("login"),
                                                        (String) b.get("password"));
                        if (!ok) {
                            status = 400;
                            response = "{\"error\":\"invalid data\"}";
                        } else {
                            status = 201;
                            response = "{\"status\":\"created\"}";
                        }
                    }
                }

                // ============ GET ============
                case "GET" -> {
                    status = 501;
                    response = "{\"error\":\"not implemented yet\"}";
                }

                // ============ PUT ============
                case "PUT" -> {
                    status = 501;
                    response = "{\"error\":\"not implemented yet\"}";
                }

                default -> {
                    status = 405;
                    response = "{\"error\":\"method not allowed\"}";
                }
            }
        } catch (Exception e) {
            status = 500;
            response = "{\"error\":\"" + e.getMessage() + "\"}";
        }

        sendJson(ex, status, response);
    }

    // ---------- helpers ----------

    private String extract(String q, String key) {
        if (q == null) return "";
        for (String p : q.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return (Map<String, Object>) JsonUtils.fromJson(body, Map.class);
    }

    private void sendJson(HttpExchange ex, int status, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
