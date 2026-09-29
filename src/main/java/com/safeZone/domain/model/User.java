package com.safezone.domain.model;

import com.safezone.domain.enums.Role;
import com.safezone.domain.enums.UserStatus;

public class User {

    private final int userId;
    private String login;
    private String passwordHash;
    private UserStatus status;
    private Role role;

    public User(
            int userId,
            String login,
            String passwordHash,
            UserStatus status,
            Role role
    ) {
        this.userId = userId;
        this.login = login;
        this.passwordHash = passwordHash;
        this.status = status;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Role getRole() {
        return role;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}