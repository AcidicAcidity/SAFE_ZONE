package com.safezone.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import com.safezone.domain.enums.UserStatus;

public record UserStatusRequest(@NotBlank UserStatus status) {}
