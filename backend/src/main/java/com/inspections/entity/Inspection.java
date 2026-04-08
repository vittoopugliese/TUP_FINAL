package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Inspection.
 * Estados: PENDING, IN_PROGRESS, DONE_FAILED, DONE_COMPLETED.
 */
@Entity
@Table(name = "inspections", indexes = {
    @Index(name = "idx_inspection_building", columnList = "buildingId"),
    @Index(name = "idx_inspection_location", columnList = "locationId"),
    @Index(name = "idx_inspection_status",   columnList = "status"),
    @Index(name = "idx_inspection_date",     columnList = "scheduledDate")
})
public class Inspection {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** FK hacia el edificio (entidad externa / ID externo). */
    private String buildingId;

    /** FK hacia la ubicación (Location). */
    private String locationId;

    /** Tipo: Daily, Weekly, Monthly, Annually. */
    private String type;

    /** Estado: PENDING, IN_PROGRESS, DONE_FAILED, DONE_COMPLETED. */
    @Column(nullable = false)
    private String status = "PENDING";

    private Instant scheduledDate;
    private Instant approvalDate;

    /** Resultado al finalizar: SUCCESS | FAILED. */
    private String result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String signer;
    private boolean signed = false;
    private Instant signDate;
    private Instant startedAt;

    private String inspectionReportId;
    private String inspectionTemplateId;
    private String coverPageId;

    /** Email (minúsculas) del usuario autenticado que creó la fila de inspección. */
    @Column(name = "created_by_email", length = 255)
    private String createdByEmail;

    private Instant createdAt;
    private Instant updatedAt;

    public Inspection() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public Instant getApprovalDate() { return approvalDate; }
    public void setApprovalDate(Instant approvalDate) { this.approvalDate = approvalDate; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getSigner() { return signer; }
    public void setSigner(String signer) { this.signer = signer; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public Instant getSignDate() { return signDate; }
    public void setSignDate(Instant signDate) { this.signDate = signDate; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public String getInspectionReportId() { return inspectionReportId; }
    public void setInspectionReportId(String v) { this.inspectionReportId = v; }

    public String getInspectionTemplateId() { return inspectionTemplateId; }
    public void setInspectionTemplateId(String v) { this.inspectionTemplateId = v; }

    public String getCoverPageId() { return coverPageId; }
    public void setCoverPageId(String v) { this.coverPageId = v; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
