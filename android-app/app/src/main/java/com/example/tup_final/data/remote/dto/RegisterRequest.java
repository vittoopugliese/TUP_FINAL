package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la request de registro.
 * Coincide con RegisterRequest del backend: { email, fullName, role, password }.
 */
public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("role")
    private String role;

    @SerializedName("password")
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String fullName, String role, String password) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
