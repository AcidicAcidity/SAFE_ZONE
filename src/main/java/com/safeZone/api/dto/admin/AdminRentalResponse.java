package com.safezone.api.dto.admin;

import java.time.LocalDateTime;
import com.safezone.domain.model.Rental;

public record AdminRentalResponse(int id, int userId, int cellId, LocalDateTime startDateTime, LocalDateTime endDateTime, String status) {
    public static AdminRentalResponse from (Rental r) {
        return new AdminRentalResponse(r.getRentalId(), r.getUserId(), r.getCellId(), r.getStartDateTime(), r.getEndDateTime(), r.getStatus().name());
    }
}
