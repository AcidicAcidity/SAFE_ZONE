package com.safezone.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String login, @NotBlank String password) {}
