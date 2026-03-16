package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO para la inspección creada.
 */
public class CreateInspectionResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("buildingId")
    private String buildingId;

    @SerializedName("buildingName")
    private String buildingName;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private String status;

    @SerializedName("scheduledDate")
    private String scheduledDate;

    @SerializedName("inspectionTemplateId")
    private String inspectionTemplateId;

    @SerializedName("notes")
    private String notes;

    @SerializedName("locationsCount")
    private int locationsCount;

    @SerializedName("zonesCount")
    private int zonesCount;

    @SerializedName("devicesCount")
    private int devicesCount;

    @SerializedName("testsCount")
    private int testsCount;

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

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

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
