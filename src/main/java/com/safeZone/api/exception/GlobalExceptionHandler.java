package com.safezone.api.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return ResponseEntity.status(404).body(ApiError.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e) {
        return ResponseEntity.status(409).body(ApiError.of(HttpStatus.CONFLICT, e.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> badRequest(Exception e) {
        return ResponseEntity.status(400).body(ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiError> db(SQLException e) {
        return ResponseEntity.status(500)
            .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
