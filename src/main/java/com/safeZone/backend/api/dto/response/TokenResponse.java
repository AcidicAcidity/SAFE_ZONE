package com.safezone.backend.api.dto.response;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
