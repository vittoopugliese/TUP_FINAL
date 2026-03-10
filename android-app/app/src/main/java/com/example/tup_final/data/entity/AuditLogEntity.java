package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para AuditLog.
 * Entidad propuesta para trazabilidad. Registra las acciones relevantes del usuario
 * para cumplimiento y auditoría.
 *
 * Eventos a auditar: firma de inspección, finalización de test, creación/actualización/eliminación de observaciones
 * (especialmente Deficiencias), cambios de estado de inspección, creación de Location/Zone/Device.
 */

@Entity(
    tableName = "audit_logs",
    indices = { @Index("userId"), @Index("entityType"), @Index("entityId"), @Index("createdAt") }
)

public class AuditLogEntity {
    @PrimaryKey
    @NonNull
    public String id;
    /** FK para el usuario */
    public String userId;
    /** Tipo de entidad: Inspection, Test, Step, Observation, Location, Zone, Device, etc. */
    public String entityType;
    /** ID de la entidad afectada. */
    public String entityId;
    /** Accion: CREATE, UPDATE, DELETE, SIGN, FINALIZE. */
    public String action;
    /** Datos adicionales (JSON: payload, IP, etc.). */
    public String metadataJson;
    /** Marca de tiempo (ISO string). */
    public String createdAt;

    public AuditLogEntity() {}

    public AuditLogEntity(String id, String userId, String entityType, String entityId, String action, String metadataJson, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.metadataJson = metadataJson;
        this.createdAt = createdAt;
    }
}