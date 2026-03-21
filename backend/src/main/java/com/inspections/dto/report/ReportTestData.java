package com.inspections.dto.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO interno para tests en el reporte PDF.
 */
public class ReportTestData {

    private String id;
    private String name;
    private String description;
    private String status;
    private Instant createdAt;
    private List<ReportStepData> steps = new ArrayList<>();

    public ReportTestData() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<ReportStepData> getSteps() { return steps; }
    public void setSteps(List<ReportStepData> steps) { this.steps = steps != null ? steps : new ArrayList<>(); }
}
