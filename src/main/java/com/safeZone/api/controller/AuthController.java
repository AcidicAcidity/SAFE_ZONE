package com.safezone.api.controller;

import com.safezone.api.dto.request.LoginRequest;
import com.safezone.api.dto.request.RegisterRequest;
import com.safezone.api.dto.response.TokenResponse;
import com.safezone.api.dto.response.UserResponse;
import com.safezone.api.security.JwtService;
import com.safezone.application.exception.UserNotFoundException;
import com.safezone.application.service.AuthService;
import com.safezone.application.service.UserService;
import com.safezone.domain.model.User;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, UserService userService ,JwtService jwtService) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req.login(), req.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        User user = authService.login(req.login(), req.password());
        String token = jwtService.generate(user.getUserId(), user.getRole().name());
        return new TokenResponse(token, "Bearer", jwtService.getTtlSeconds());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Integer userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь не найден.");
        }
        return UserResponse.from(user);
    }
}
