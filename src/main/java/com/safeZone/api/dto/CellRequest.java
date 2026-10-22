package com.safezone.api.dto;

import jakarta.validation.constraints.NotBlank;

// Не указал status НАМЕРЕНО!!!! Так как в БД поставиться по умолчанию ACTIVE
public record CellRequest(@NotBlank int warehouseId, @NotBlank int row, @NotBlank int column, @NotBlank String size) {}
