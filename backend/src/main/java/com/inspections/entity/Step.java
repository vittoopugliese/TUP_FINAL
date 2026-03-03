package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Step (Test Step).
 * Elemento individual de un Test que requiere entrada de datos o validación.
 * Tipos: BINARY, RANGE, SIMPLE_VALUE, MULTI_VALUE, AUTOMATIC.
 * Estados: PENDING, SUCCESS, FAILED.
 */
@Entity
@Table(name = "steps", indexes = {
    @Index(name = "idx_step_test",   columnList = "testId"),
    @Index(name = "idx_step_status", columnList = "status")
})
public class Step {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** FK hacia InspectionTest. */
    @Column(nullable = false)
    private String testId;

    @Column(nullable = false)
    private String name;

    /** Tipo: BINARY, RANGE, SIMPLE_VALUE, MULTI_VALUE, AUTOMATIC. */
    private String testStepType;

    private boolean applicable = true;

    /** Estado: PENDING, SUCCESS, FAILED. */
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Valor almacenado como JSON. Estructura según testStepType:
     * BINARY: {"value": bool}, RANGE: {min/max}, SIMPLE_VALUE, MULTI_VALUE, AUTOMATIC.
     */
    @Column(columnDefinition = "TEXT")
    private String valueJson;

    private Double minValue;
    private Double maxValue;

    private Instant createdAt;
    private Instant updatedAt;

    public Step() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTestStepType() { return testStepType; }
    public void setTestStepType(String v) { this.testStepType = v; }

    public boolean isApplicable() { return applicable; }
    public void setApplicable(boolean applicable) { this.applicable = applicable; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getValueJson() { return valueJson; }
    public void setValueJson(String valueJson) { this.valueJson = valueJson; }

    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }

    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
