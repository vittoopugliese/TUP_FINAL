package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para actualizar el rol de un usuario.
 * Solo permite INSPECTOR u OPERATOR.
 */
public class UpdateRoleRequest {

    @NotBlank(message = "El rol es requerido")
    @Pattern(regexp = "INSPECTOR|OPERATOR", message = "Rol inválido. Solo INSPECTOR u OPERATOR.")
    private String role;

    public UpdateRoleRequest() {}

    public UpdateRoleRequest(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
