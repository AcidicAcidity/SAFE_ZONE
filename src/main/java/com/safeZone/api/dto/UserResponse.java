package com.safezone.api.dto;

import com.safezone.domain.model.User;

public record UserResponse(int id, String login, String status, String Role) {
    public static UserResponse from (User u) {
        return new UserResponse(u.getUserId(), u.getLogin(), u.getStatus().name(), u.getRole().name());
    }
}
