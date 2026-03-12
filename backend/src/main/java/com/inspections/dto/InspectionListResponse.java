package com.inspections.dto;

import java.time.Instant;

/**
 * DTO para la lista de inspecciones.
 * Contiene los campos necesarios para mostrar las tarjetas en la app.
 */
public class InspectionListResponse {

    private String id;
    private String buildingId;
    private String status;
    private Instant scheduledDate;
    private String type;

    public InspectionListResponse() {}

    public InspectionListResponse(String id, String buildingId, String status,
                                   Instant scheduledDate, String type) {
        this.id = id;
        this.buildingId = buildingId;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
