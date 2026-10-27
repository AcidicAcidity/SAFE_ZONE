package com.safezone.api.controller;

import com.safezone.api.dto.response.UserResponse;
import com.safezone.api.dto.request.UserStatusRequest;
import com.safezone.application.service.UserService;

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

/**
 * СКЕЛЕТ: профиль и управление пользователями (Endpoint.md, разделы 2 и 3).
 *
 * /users/me и /users/{userId} живут в одном контроллере: Spring предпочитает
 * точное совпадение "/me" шаблону "/{userId}", так что конфликта нет.
 *
 * Доступ задан не в SecurityConfig, а над методами: GET /users/me требует просто токен
 * (anyRequest().authenticated()), остальное @PreAuthorize("hasRole('ADMIN')").
 *
 * Напиши DTO:
 *   UserResponse        уже есть (id, login, status, role): хеша пароля в нём нет
 *   UserStatusRequest   record UserStatusRequest(@NotNull UserStatus status) {}
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // GET /api/v1/users/me (любой авторизованный)
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Integer userId) {
        // TODO: то же, что AuthController.me (найти пользователя, null -> UserNotFoundException)
        throw new UnsupportedOperationException("TODO");
    }

    // GET /api/v1/users (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        // TODO: service.getUsers() -> UserResponse::from. Именно UserResponse, не модель User!
        throw new UnsupportedOperationException("TODO");
    }

    // GET /api/v1/users/7 (ADMIN)
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getById(@PathVariable int userId) {
        // TODO: service.findUserById(userId); null -> 404
        throw new UnsupportedOperationException("TODO");
    }

    // PATCH /api/v1/users/7/status (ADMIN)   тело: {"status": "BLOCKED"}
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeStatus(
            @PathVariable int userId,
            @Valid @RequestBody UserStatusRequest req,
            @AuthenticationPrincipal Integer currentAdminId) {

        // TODO:
        //   1. Защита от самоблокировки: если userId == currentAdminId -> IllegalArgumentException
        //      (или ConflictException). Иначе единственный админ заблокирует сам себя.
        //   2. В сервисе есть три метода, выбери нужный через switch по req.status():
        //        switch (req.status()) {
        //            case BLOCKED -> service.blockUser(userId);
        //            case ACTIVE  -> service.unblockUser(userId);
        //            case DELETED -> service.deleteUser(userId);
        //            default      -> throw new IllegalArgumentException("Недопустимый статус.");
        //        }
        //   3. Правило переходов: из DELETED в ACTIVE выходить нельзя (удаление логическое, но необратимое).
        //      Проверку лучше положить в сервис, а не в контроллер.
        //   4. Вернуть UserResponse.from(service.findUserById(userId)).
        throw new UnsupportedOperationException("TODO");
    }

    // =====================================================================
    // ЗАДАНИЯ СО ЗВЁЗДОЧКОЙ (Endpoint.md: НЕ РЕАЛИЗОВАНО / ОТЛОЖЕНО)
    //
    // PATCH /users/me           сменить профиль. Сначала реши, ЧТО можно менять (логин?).
    //                           Нужен UserRepository.updateLogin и проверка на дубликат (409).
    // PATCH /users/{id}/role    ADMIN. Тело {"role":"ADMIN"}. UserRepository.updateRole(int, Role).
    //                           Помни: роль зашита в уже выданные токены, она сменится только
    //                           после их истечения (или входа заново).
    // PATCH /users/me/password  ОТЛОЖЕНО: старый пароль + новый, passwordService.matches/hash.
    // =====================================================================
}
