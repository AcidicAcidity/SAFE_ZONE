package com.safezone.api.dto;

import java.time.LocalDateTime;

import com.safezone.domain.model.Rental;

public record RentalResponse(int id, int userId, int cellId, LocalDateTime startRent, LocalDateTime endRent, String status) {
    public static RentalResponse from (Rental r) {
        return new RentalResponse(r.getRentalId(), r.getUserId(), r.getCellId(), r.getStartDateTime(), r.getEndDateTime(), r.getStatus().name());
    }
}
