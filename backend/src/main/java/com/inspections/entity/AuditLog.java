package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para AuditLog.
 * Trazabilidad: registra acciones relevantes del usuario (firma, finalización de test,
 * creación/actualización/eliminación de observaciones, cambios de estado, etc.)
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user",        columnList = "userId"),
    @Index(name = "idx_audit_entity_type", columnList = "entityType"),
    @Index(name = "idx_audit_entity_id",   columnList = "entityId"),
    @Index(name = "idx_audit_created",     columnList = "createdAt")
})
public class AuditLog {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** FK hacia User. */
    @Column(nullable = false)
    private String userId;

    /** Tipo de entidad: Inspection, Test, Step, Observation, Location, Zone, Device, … */
    @Column(nullable = false)
    private String entityType;

    /** ID de la entidad afectada. */
    @Column(nullable = false)
    private String entityId;

    /** Acción: CREATE, UPDATE, DELETE, SIGN, FINALIZE. */
    @Column(nullable = false)
    private String action;

    /** Datos adicionales como JSON (payload, IP, etc.). */
    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false)
    private Instant createdAt;

    public AuditLog() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
