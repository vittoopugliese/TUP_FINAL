package com.inspections.entity;

import jakarta.persistence.*;

/**
 * Step definition for a test template. Cloned into concrete Step when creating an InspectionTest.
 */
@Entity
@Table(name = "test_template_steps", indexes = {
    @Index(name = "idx_tts_template", columnList = "test_template_id")
})
public class TestTemplateStep {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "test_template_id", nullable = false, length = 36)
    private String testTemplateId;

    @Column(name = "name")
    private String name;

    @Column(name = "test_step_type", length = 50)
    private String testStepType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public TestTemplateStep() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTestTemplateId() { return testTemplateId; }
    public void setTestTemplateId(String testTemplateId) { this.testTemplateId = testTemplateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTestStepType() { return testStepType; }
    public void setTestStepType(String testStepType) { this.testStepType = testStepType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }

    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
