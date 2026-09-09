package com.safeZone.util;

import com.safeZone.model.Payment;
import java.time.LocalDateTime;
import org.slf4j.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PaymentProvider {
    private static final Logger log = LoggerFactory.getLogger(PaymentProvider.class);

    private final DBHelper dbHelper;

    public PaymentProvider(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void createPayment(LocalDateTime rentTime, String size, String token) throws Exception {
        log.info("Создание платежа: " + rentTime + " h " + size + " size");
        int binId = dbHelper.findFreeBin(size, rentTime);
        if (binId <= 0) {
            log.error("Нет свободных ячеек для размера: " + size);
            throw new Exception("Нет свободных ячеек для размера: " + size);
        }
        int paymentId = dbHelper.createPayment(rentTime, size, binId, token);
        log.info("Платеж создан: " + paymentId);
    }

    public String getPaymentStatus(int paymentId) {
        return dbHelper.getPaymentStatus(paymentId);
    }

    public List<Map<String, Object>> getPayments(LocalDateTime rentTime, LocalDateTime createdAt, LocalDateTime endRentDate, int binId, int userId) {
        return dbHelper.getPayments(rentTime, createdAt, endRentDate, binId, userId);
    }
}
