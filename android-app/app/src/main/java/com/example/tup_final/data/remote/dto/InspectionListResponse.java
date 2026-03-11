package com.example.tup_final.data.remote.dto;

/**
 * DTO para la lista de inspecciones.
 * Coincide con InspectionListResponse del backend.
 */
public class InspectionListResponse {

    private String id;
    private String buildingId;
    private String status;
    private String scheduledDate;
    private String type;

    public InspectionListResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
