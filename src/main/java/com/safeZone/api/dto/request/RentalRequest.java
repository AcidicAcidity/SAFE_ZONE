package com.safezone.api.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.safezone.domain.enums.CellSize;

public record RentalRequest(Integer cellId, CellSize size, @NotBlank LocalDateTime startDateTime, @Min(1) @Max(24) int hours) {}
