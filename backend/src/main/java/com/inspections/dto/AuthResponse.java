package com.inspections.dto;

/**
 * Response de login/refresh: token JWT + metadata del usuario.
 */
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private String email;
    private String role;
    private String userId;
    private String fullName;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String role, String userId, String fullName) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
