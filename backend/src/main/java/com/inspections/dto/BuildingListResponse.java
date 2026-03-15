package com.inspections.dto;

/**
 * DTO para el catálogo de buildings.
 */
public class BuildingListResponse {

    private String id;
    private String name;
    private String details;

    public BuildingListResponse() {}

    public BuildingListResponse(String id, String name, String details) {
        this.id = id;
        this.name = name;
        this.details = details;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
