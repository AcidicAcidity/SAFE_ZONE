package com.safezone.application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordService {
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String password){
        return encoder.encode(password);
    }

    public boolean matches(String password, String passwordHash) {
        return encoder.matches(password, passwordHash);
    }
}
