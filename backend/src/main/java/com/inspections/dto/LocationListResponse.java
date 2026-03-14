package com.inspections.dto;

/**
 * DTO para la lista de ubicaciones.
 * Contiene los campos necesarios para mostrar las cards en la app.
 */
public class LocationListResponse {

    private String id;
    private String buildingId;
    private String name;
    private String details;

    public LocationListResponse() {}

    public LocationListResponse(String id, String buildingId, String name, String details) {
        this.id = id;
        this.buildingId = buildingId;
        this.name = name;
        this.details = details;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
