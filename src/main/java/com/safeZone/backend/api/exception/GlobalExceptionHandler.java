package com.safezone.backend.api.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;


import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;

import com.safezone.backend.application.exception.AuthenticationException;
import com.safezone.backend.application.exception.UserAlreadyExistsException;
import com.safezone.backend.application.exception.UserNotFoundException;
import com.safezone.backend.application.exception.CellUnavailableException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<ApiError> notFound(RuntimeException e) {
        return ResponseEntity.status(404).body(ApiError.of(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler({ConflictException.class, UserAlreadyExistsException.class, CellUnavailableException.class})
    public ResponseEntity<ApiError> conflict(RuntimeException e) {
        return ResponseEntity.status(409).body(ApiError.of(HttpStatus.CONFLICT, e.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class, org.springframework.security.access.AccessDeniedException.class})
    public ResponseEntity<ApiError> accessDenied(RuntimeException e) {
        return ResponseEntity.status(403).body(ApiError.of(HttpStatus.FORBIDDEN, "Недостаточно прав для этого действия."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(ApiError.of(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> validation(Exception e) {
        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, "Проверьте заполнение полей запроса."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> authentication(AuthenticationException e) {
        return ResponseEntity.status(401).body(ApiError.of(HttpStatus.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler({DataBaseErrorException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> dataBase(RuntimeException e) {
        return ResponseEntity.status(500)
            .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось выполнить операцию. Повторите попытку позже."));
    }

}
