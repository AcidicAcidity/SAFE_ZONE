package com.safezone.backend.api.controller;

import com.safezone.backend.api.dto.response.UserResponse;
import com.safezone.backend.api.dto.request.UserStatusRequest;
import com.safezone.backend.api.exception.NotFoundException;
import com.safezone.backend.api.exception.DataBaseErrorException;
import com.safezone.backend.api.exception.ConflictException;
import com.safezone.backend.application.service.UserService;
import com.safezone.backend.domain.model.User;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Integer userId) {
        try {
            User user = service.findUserById(userId);
            return UserResponse.from(user);
        } catch (IllegalStateException e) {
            throw new NotFoundException("Не удалось найти пользователя");
        }
    }

    // GET /api/v1/users (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        try {
            return service.getUsers().stream()
                .map(UserResponse::from)
                .toList();
        } catch (IllegalStateException e) {
            throw new DataBaseErrorException("Не удалось получить список пользователей");
        }
    }

    // GET /api/v1/users/7 (ADMIN)
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(@PathVariable int userId) {
        try {
            User user = service.findUserById(userId);
            return UserResponse.from(user);
        } catch (IllegalStateException e) {
            throw new NotFoundException("Не удалось найти пользователя с " + userId + "id.");
        }
    }

    // PATCH /api/v1/users/7/status (ADMIN)   тело: {"status": "BLOCKED"}
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeStatus(
            @PathVariable int userId,
            @Valid @RequestBody UserStatusRequest req,
            @AuthenticationPrincipal Integer currentAdminId) {

            if (userId == currentAdminId) {
                throw new ConflictException("Вы не можете заблокировать или удалить свой аккаунт");
            }

            switch (req.status()) {
                case BLOCKED -> service.blockUser(userId);
                case ACTIVE -> service.unblockUser(userId);
                case DELETED -> service.deleteUser(userId);
                default -> throw new IllegalArgumentException("Недопустимый статус.");
            }

            User user = service.findUserById(userId);
            return UserResponse.from(user);
    }
}
