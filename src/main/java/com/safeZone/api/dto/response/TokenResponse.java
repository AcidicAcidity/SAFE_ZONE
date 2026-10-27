package com.safezone.api.dto.response;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
