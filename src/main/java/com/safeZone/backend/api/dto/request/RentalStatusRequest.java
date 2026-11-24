package com.safezone.backend.api.dto.request;

import jakarta.validation.constraints.NotNull;

import com.safezone.backend.domain.enums.RentalStatus;

public record RentalStatusRequest(@NotNull RentalStatus status) {}
