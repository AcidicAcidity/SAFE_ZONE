package com.safezone.domain.model;

import com.safezone.domain.enums.RentalStatus;

import java.time.LocalDateTime;

public class Rental {

    private final int rentalId;
    private final int userId;
    private final int cellId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private RentalStatus status;

    public Rental(int rentalId,
                  int userId,
                  int cellId,
                  LocalDateTime startDateTime,
                  LocalDateTime endDateTime,
                  RentalStatus status) {

        this.rentalId = rentalId;
        this.userId = userId;
        this.cellId = cellId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.status = status;
    }

    public int getRentalId() {
        return rentalId;
    }

    public int getUserId() {
        return userId;
    }

    public int getCellId() {
        return cellId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }
}