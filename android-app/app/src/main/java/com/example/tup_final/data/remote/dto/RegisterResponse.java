package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la response de registro.
 * Coincide con RegisterResponse del backend: { message, email }.
 */
public class RegisterResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("email")
    private String email;

    public RegisterResponse() {
    }

    public RegisterResponse(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
