package com.safezone.api.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(@NotBlank String name, @NotBlank String address) {}
