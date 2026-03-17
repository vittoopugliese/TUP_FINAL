package com.inspections.dto;

/**
 * DTO para Step en respuestas API.
 */
public class StepResponse {
    private String id;
    private String testId;
    private String name;
    private String testStepType;
    private boolean applicable;
    private String status;
    private String description;
    private String valueJson;
    private Double minValue;
    private Double maxValue;
    private String createdAt;
    private String updatedAt;

    public StepResponse() {}

    public StepResponse(String id, String testId, String name, String testStepType,
                        boolean applicable, String status, String description,
                        String valueJson, Double minValue, Double maxValue,
                        String createdAt, String updatedAt) {
        this.id = id;
        this.testId = testId;
        this.name = name;
        this.testStepType = testStepType;
        this.applicable = applicable;
        this.status = status;
        this.description = description;
        this.valueJson = valueJson;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }
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
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
