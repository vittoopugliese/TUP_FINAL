package com.inspections.dto;

import java.time.Instant;

/**
 * Response DTO para la inspección creada.
 */
public class CreateInspectionResponse {

    private String id;
    private String buildingId;
    private String buildingName;
    private String type;
    private String status;
    private Instant scheduledDate;
    private String inspectionTemplateId;
    private String notes;
    private int locationsCount;
    private int zonesCount;
    private int devicesCount;
    private int testsCount;

    public CreateInspectionResponse() {}

    public CreateInspectionResponse(String id, String buildingId, String buildingName, String type,
                                    String status, Instant scheduledDate, String inspectionTemplateId,
                                    String notes, int locationsCount, int zonesCount,
                                    int devicesCount, int testsCount) {
        this.id = id;
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.type = type;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.inspectionTemplateId = inspectionTemplateId;
        this.notes = notes;
        this.locationsCount = locationsCount;
        this.zonesCount = zonesCount;
        this.devicesCount = devicesCount;
        this.testsCount = testsCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getInspectionTemplateId() { return inspectionTemplateId; }
    public void setInspectionTemplateId(String inspectionTemplateId) { this.inspectionTemplateId = inspectionTemplateId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getLocationsCount() { return locationsCount; }
    public void setLocationsCount(int locationsCount) { this.locationsCount = locationsCount; }

    public int getZonesCount() { return zonesCount; }
    public void setZonesCount(int zonesCount) { this.zonesCount = zonesCount; }

    public int getDevicesCount() { return devicesCount; }
    public void setDevicesCount(int devicesCount) { this.devicesCount = devicesCount; }

    public int getTestsCount() { return testsCount; }
    public void setTestsCount(int testsCount) { this.testsCount = testsCount; }
}
