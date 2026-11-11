package com.safezone.backend.application.exception;

public class CellUnavailableException
        extends RuntimeException {

    public CellUnavailableException(String message) {
        super(message);
    }
}
