package com.safezone.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordService {

    public String hash(String password) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            password.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 недоступен.",
                    e
            );
        }
    }

    public boolean matches(
            String password,
            String passwordHash) {

        return hash(password).equals(passwordHash);
    }
}