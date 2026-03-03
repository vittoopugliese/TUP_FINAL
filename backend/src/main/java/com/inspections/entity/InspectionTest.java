package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Test (procedimiento de verificación).
 * Renombrada InspectionTest para evitar conflicto con la palabra reservada SQL
 * "TEST".
 * Estados: PENDING, COMPLETED, FAILED.
 */
@Entity
@Table(name = "tests", indexes = {
        @Index(name = "idx_test_device", columnList = "deviceId"),
        @Index(name = "idx_test_inspection", columnList = "inspectionId"),
        @Index(name = "idx_test_status", columnList = "status")
})
public class InspectionTest {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** FK hacia Device. */
    @Column(nullable = false)
    private String deviceId;

    /** FK hacia Inspection. */
    @Column(nullable = false)
    private String inspectionId;

    private String testTemplateId;

    /** IDs de Steps en orden (JSON array). */
    @Column(columnDefinition = "TEXT")
    private String testStepIds;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Estado: PENDING, COMPLETED, FAILED. */
    @Column(nullable = false)
    private String status = "PENDING";

    private boolean applicable = true;

    private Instant createdAt;
    private Instant updatedAt;

    public InspectionTest() {
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(String inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getTestTemplateId() {
        return testTemplateId;
    }

    public void setTestTemplateId(String v) {
        this.testTemplateId = v;
    }

    public String getTestStepIds() {
        return testStepIds;
    }

    public void setTestStepIds(String v) {
        this.testStepIds = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isApplicable() {
        return applicable;
    }

    public void setApplicable(boolean applicable) {
        this.applicable = applicable;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
