package com.safezone.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.safezone.backend.domain.enums.UserStatus;

public record UserStatusRequest(@NotBlank UserStatus status) {}
