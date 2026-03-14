package com.example.tup_final.data.remote.dto;

/**
 * DTO para respuesta de asignacion.
 * createdAt viene como ISO string desde el backend.
 */
public class AssignmentResponse {

    private String id;
    private String inspectionId;
    private String userEmail;
    private String role;
    private String createdAt;

    public AssignmentResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
