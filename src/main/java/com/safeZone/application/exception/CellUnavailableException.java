package com.safezone.application.exception;

public class CellUnavailableException
        extends RuntimeException {

    public CellUnavailableException(String message) {
        super(message);
    }
}