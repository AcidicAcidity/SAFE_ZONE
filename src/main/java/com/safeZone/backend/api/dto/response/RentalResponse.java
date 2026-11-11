package com.safezone.backend.api.dto.response;

import java.time.LocalDateTime;

import com.safezone.backend.domain.model.Rental;

public record RentalResponse(int id, int cellId, LocalDateTime startRent, LocalDateTime endRent, String status) {
    public static RentalResponse from (Rental r) {
        return new RentalResponse(r.getRentalId(), r.getCellId(), r.getStartDateTime(), r.getEndDateTime(), r.getStatus().name());
    }
}
