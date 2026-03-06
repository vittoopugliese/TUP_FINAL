package com.example.tup_final.data.remote.dto;

/**
 * DTO para la response de login/refresh.
 * Coincide con AuthResponse del backend:
 * { token, type, email, role, userId, fullName }.
 */
public class LoginResponse {

    private String token;
    private String type;
    private String email;
    private String role;
    private String userId;
    private String fullName;

    public LoginResponse() {
    }

    public LoginResponse(String token, String type, String email,
            String role, String userId, String fullName) {
        this.token = token;
        this.type = type;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
