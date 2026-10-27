package com.safezone.api.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String m) { super(m); }
}
