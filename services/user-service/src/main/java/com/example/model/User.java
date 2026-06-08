package com.example.model;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import jakarta.validation.constraints.*;
import jakarta.persistence.*;

import java.util.UUID;

@Node("User")
@Data
public class User {
    @Id
    private String id;

    @NotBlank @Size(max = 50, message = "Username too long")
    private String username;

    @NotBlank @Email(message = "Email should be valid") @Size(max = 255, message = "Email too long")
    private String email;

    @NotBlank @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;
    private String twoFactorSecret;
    private boolean twoFactorEnabled;

    public User() {
    }

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        this.twoFactorSecret = twoFactorSecret;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }
}