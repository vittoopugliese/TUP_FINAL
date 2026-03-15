package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para agregar una asignacion a una inspeccion.
 */
public class AssignmentRequest {

    @NotBlank(message = "userEmail is required")
    private String userEmail;

    @NotBlank(message = "role is required")
    private String role;

    public AssignmentRequest() {}

    public AssignmentRequest(String userEmail, String role) {
        this.userEmail = userEmail;
        this.role = role;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
