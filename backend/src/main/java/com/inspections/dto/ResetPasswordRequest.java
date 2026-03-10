package com.inspections.dto;

import com.inspections.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

/**
 * Request para restablecer la contraseña.
 * Requiere el token de recuperación y la nueva contraseña.
 */
public class ResetPasswordRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @ValidPassword
    private String newPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
