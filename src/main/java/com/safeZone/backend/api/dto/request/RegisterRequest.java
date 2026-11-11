package com.safezone.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(@NotBlank String login, @NotBlank String password) {}
