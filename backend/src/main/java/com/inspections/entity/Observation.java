package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Observation.
 * Nota, recomendación o deficiencia adjunta a un Step.
 * Tipos: RECOMMENDATIONS, REMARKS, DEFICIENCIES.
 */
@Entity
@Table(name = "observations", indexes = {
    @Index(name = "idx_obs_step",       columnList = "testStepId"),
    @Index(name = "idx_obs_inspection", columnList = "inspectionId"),
    @Index(name = "idx_obs_type",       columnList = "type")
})
public class Observation {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** FK hacia Step. */
    @Column(nullable = false)
    private String testStepId;

    /** FK hacia Inspection. */
    private String inspectionId;

    /** Nombre a mostrar en UI. */
    private String name;

    /** Tipo: RECOMMENDATIONS, REMARKS, DEFICIENCIES. */
    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Requerido si type=DEFICIENCIES. */
    private String deficiencyTypeId;

    /** FK hacia Photo/Media. */
    private String mediaId;

    private Instant createdAt;
    private Instant updatedAt;

    public Observation() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTestStepId() { return testStepId; }
    public void setTestStepId(String testStepId) { this.testStepId = testStepId; }

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDeficiencyTypeId() { return deficiencyTypeId; }
    public void setDeficiencyTypeId(String v) { this.deficiencyTypeId = v; }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
