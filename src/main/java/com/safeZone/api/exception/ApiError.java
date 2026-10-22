package com.safezone.api.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;

public record ApiError(int status, String error, String message, Instant timestamp) {
    public static ApiError of(HttpStatus s, String msg) {
        return new ApiError(s.value(), s.getReasonPhrase(), msg, Instant.now());
    }
}
