package com.inspections.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request para actualizar un Step (PATCH /api/steps/{stepId}).
 */
public class UpdateStepRequest {
    /** Valor en JSON según tipo de step. */
    private String valueJson;
    /** Si el step aplica (N/A cuando false). */
    private Boolean applicable;

    public UpdateStepRequest() {}

    public String getValueJson() { return valueJson; }
    public void setValueJson(String valueJson) { this.valueJson = valueJson; }
    public Boolean getApplicable() { return applicable; }
    public void setApplicable(Boolean applicable) { this.applicable = applicable; }
}
