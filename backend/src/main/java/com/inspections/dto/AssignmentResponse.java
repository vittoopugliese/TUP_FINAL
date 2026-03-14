package com.inspections.dto;

import java.time.Instant;

/**
 * DTO para respuesta de asignacion.
 */
public class AssignmentResponse {

    private String id;
    private String inspectionId;
    private String userEmail;
    private String role;
    private Instant createdAt;

    public AssignmentResponse() {}

    public AssignmentResponse(String id, String inspectionId, String userEmail, String role, Instant createdAt) {
        this.id = id;
        this.inspectionId = inspectionId;
        this.userEmail = userEmail;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
