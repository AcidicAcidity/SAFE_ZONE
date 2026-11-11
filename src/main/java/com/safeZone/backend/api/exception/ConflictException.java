package com.safezone.backend.api.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String m) { super(m); }
}
