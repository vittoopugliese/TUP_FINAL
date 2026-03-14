package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para asignaciones de usuarios a inspecciones.
 * Relaciona una inspeccion con un usuario (por email) y su rol: INSPECTOR u OPERATOR.
 * Reglas: max 1 INSPECTOR por inspeccion, operadores ilimitados.
 */
@Entity
@Table(name = "inspection_assignments", indexes = {
    @Index(name = "idx_assign_inspection", columnList = "inspectionId"),
    @Index(name = "idx_assign_email", columnList = "userEmail")
})
public class InspectionAssignment {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String inspectionId;

    @Column(nullable = false, length = 255)
    private String userEmail;

    /** Rol: INSPECTOR | OPERATOR */
    @Column(nullable = false, length = 50)
    private String role;

    private Instant createdAt;

    public InspectionAssignment() {}

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
