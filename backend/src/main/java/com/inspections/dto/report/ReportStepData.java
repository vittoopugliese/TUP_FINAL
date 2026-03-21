package com.inspections.dto.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO interno para steps en el reporte PDF.
 */
public class ReportStepData {

    private String id;
    private String name;
    private String testStepType;
    private boolean applicable;
    private String status;
    private String description;
    private String valueJson;
    private Double minValue;
    private Double maxValue;
    private Instant createdAt;
    private List<ReportObservationData> observations = new ArrayList<>();

    public ReportStepData() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTestStepType() { return testStepType; }
    public void setTestStepType(String testStepType) { this.testStepType = testStepType; }

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

    public List<ReportObservationData> getObservations() { return observations; }
    public void setObservations(List<ReportObservationData> observations) { this.observations = observations != null ? observations : new ArrayList<>(); }
}
