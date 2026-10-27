package com.safezone.client.net;

/** An error message suitable for display to the user. */
public final class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }
}
