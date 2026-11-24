package com.safezone.backend.api.dto.request;

import jakarta.validation.constraints.NotNull;
import com.safezone.backend.domain.enums.UserStatus;

public record UserStatusRequest(@NotNull UserStatus status) {}
