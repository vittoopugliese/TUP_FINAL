package com.inspections.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request de registro de usuario.
 * Campos: email, nombre, rol (INSPECTOR | OPERATOR), contraseña.
 */
public class RegisterRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(regexp = "INSPECTOR|OPERATOR", message = "El rol debe ser INSPECTOR u OPERATOR")
    private String role;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String email, String fullName, String role, String password) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
