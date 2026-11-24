package com.safezone.backend.api.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.safezone.backend.domain.enums.CellSize;

public record RentalRequest(@Positive Integer cellId, CellSize size,
                            @NotNull LocalDateTime startDateTime, @Min(1) @Max(24) int hours) {}
