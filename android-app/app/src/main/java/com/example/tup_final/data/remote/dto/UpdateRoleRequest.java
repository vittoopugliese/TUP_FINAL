package com.example.tup_final.data.remote.dto;

/**
 * DTO para actualizar el rol de un usuario.
 * Coincide con UpdateRoleRequest del backend: { "role": "INSPECTOR" | "SUPERVISOR" }.
 */
public class UpdateRoleRequest {

    private String role;

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
