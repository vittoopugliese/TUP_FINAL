package com.example.tup_final.data.remote.dto;

/**
 * DTO para agregar una asignacion a una inspeccion.
 */
public class AssignmentRequest {

    private String userEmail;
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
