package com.example.tup_final.data.remote.dto;

/**
 * DTO para la lista de ubicaciones.
 * Coincide con LocationListResponse del backend.
 */
public class LocationListResponse {

    private String id;
    private String buildingId;
    private String name;
    private String details;

    public LocationListResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
