package com.safezone.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import com.safezone.domain.enums.RentalStatus;

public record RentalStatusRequest(@NotBlank RentalStatus status) {}
