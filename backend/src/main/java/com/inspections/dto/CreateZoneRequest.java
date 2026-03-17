package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO para crear una zona en una ubicación.
 */
public class CreateZoneRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String details;

    public CreateZoneRequest() {}

    public CreateZoneRequest(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
