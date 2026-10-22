package com.safezone.api.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
