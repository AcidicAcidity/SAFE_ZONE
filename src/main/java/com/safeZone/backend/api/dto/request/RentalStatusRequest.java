package com.safezone.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import com.safezone.backend.domain.enums.RentalStatus;

public record RentalStatusRequest(@NotBlank RentalStatus status) {}
