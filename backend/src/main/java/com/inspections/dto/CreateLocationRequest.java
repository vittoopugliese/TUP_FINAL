package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO para crear una ubicación.
 */
public class CreateLocationRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String details;
    private String buildingId;

    public CreateLocationRequest() {}

    public CreateLocationRequest(String name, String details, String buildingId) {
        this.name = name;
        this.details = details;
        this.buildingId = buildingId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }
}
