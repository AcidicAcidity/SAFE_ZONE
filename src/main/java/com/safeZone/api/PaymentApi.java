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

public class PaymentApi implements HttpHandler {

    private final SafeZoneService service;
    private final DBHelper db;

    public PaymentApi(DBHelper db) {
        this.db = db;
        this.service = new SafeZoneService(db);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String query  = ex.getRequestURI().getQuery();
        String response;
        int status = 200;

        try {
            switch (method) {
                // POST /api/payment  — создать платёж
                case "POST" -> {
                    Map<String, Object> b = readJson(ex);
                    boolean ok = service.createPayment(
                            ((Number) b.get("bin_id")).intValue(),
                            ((Number) b.get("user_id")).intValue(),
                            ((Number) b.get("priceathour")).intValue(),
                            LocalDateTime.parse((String) b.get("end_rent_date")),
                            ((Number) b.get("rentTime")).intValue());
                    if (!ok) {
                        status = 400;
                        response = "{\"error\":\"invalid payment\"}";
                    } else {
                        status = 201;
                        response = "{\"status\":\"created\"}";
                    }
                }

                // GET /api/payment?order_id=42       — проверка оплаты (isPaymentPaid)
                // GET /api/payment?id=42             — читаем напрямую
                case "GET" -> {
                    if (query != null && query.contains("order_id=")) {
                        int orderId = Integer.parseInt(extract(query, "order_id"));
                        boolean paid = Boolean.TRUE.equals(
                            service.checkEntity(EntityType.PAYMENT,
                                                java.util.Map.of("Order_ID", orderId)));
                        response = "{\"order_id\":" + orderId + ",\"paid\":" + paid + "}";
                    } else {
                        int id = Integer.parseInt(extract(query, "id"));
                        List<Map<String, Object>> rows =
                                db.getDataFromDB("SELECT * FROM payments WHERE order_id = ? LIMIT 1", id);
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
