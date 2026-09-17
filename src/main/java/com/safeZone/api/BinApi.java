package com.safeZone.api;

import com.safeZone.util.DBHelper;
import com.safeZone.util.JsonUtils;
import com.safeZone.util.SafeZoneService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.safeZone.util.EntityType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BinApi implements HttpHandler {

    private final SafeZoneService service;
    private final DBHelper db;


    public BinApi(DBHelper db) {
        this.db = db;
        this.service = new SafeZoneService(db);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path   = ex.getRequestURI().getPath();
        String query  = ex.getRequestURI().getQuery();
        String response;
        int status = 200;

        try {
            switch (method) {
                // POST /api/bin   — создать ячейку
                case "POST" -> {
                    Map<String, Object> body = readJson(ex);

                    boolean success = service.createEntity(EntityType.BIN, body);

                    if (!success) {
                        status = 400;
                        response = "{\"error\":\"invalid data\"}";
                    } else {
                        status = 201;
                        // Внимание: если сервис возвращает только true/false, то ID новой ячейки мы тут не узнаем.
                        response = "{\"success\":true}";
                    }
                }

                // GET /api/bin?id=1 — читаем напрямую
                // GET /api/bin?x=1&y=2 — через findIdByPosition
                // GET /api/bin/free?size=M&end=.. — через findFreeBin
                case "GET" -> {
                    if (path.endsWith("/free")) {
                        String size = extract(query, "size");
                        String end  = extract(query, "end");

                        // ИСПРАВЛЕНО: сервис отдает объект Bin, а не Integer
                        var freeBin = service.findFreeBin(size, LocalDateTime.parse(end));

                        if (freeBin != null) {
                            // !!! ВАЖНО: Проверь, как называется геттер ID в твоем классе Bin.
                            // Если он называется getBinId(), то поменяй .getId() на .getBinId()
                            response = "{\"id\":" + freeBin.getBin_ID() + "}";
                        } else {
                            response = "{\"id\":null}";
                        }

                    } else if (query != null && query.contains("x=") && query.contains("y=")) {
                        int x = Integer.parseInt(extract(query, "x"));
                        int y = Integer.parseInt(extract(query, "y"));
                        Integer id = service.findIdByPosition(EntityType.BIN, x, y);
                        response = id == null ? "{\"id\":null}" : "{\"id\":" + id + "}";
                    } else {
                        int id = Integer.parseInt(extract(query, "id"));
                        List<Map<String, Object>> rows =
                                db.getDataFromDB("SELECT * FROM bins WHERE bin_id = ? LIMIT 1", id);
                        if (rows.isEmpty()) { status = 404; response = "{\"error\":\"not found\"}"; }
                        else                response = JsonUtils.toJson(rows.get(0));
                    }
                }

                default -> { status = 405; response = "{\"error\":\"method not allowed\"}"; }
            }
        } catch (Exception e) {
            status = 500;
            response = "{\"error\":\"" + e.getMessage() + "\"}";
        }
        sendJson(ex, status, response);
    }

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
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }
}
