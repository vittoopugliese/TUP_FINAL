package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request DTO para crear una inspección building-wide.
 */
public class CreateInspectionRequest {

    @SerializedName("buildingId")
    private String buildingId;

    @SerializedName("type")
    private String type;

    @SerializedName("scheduledDate")
    private String scheduledDate; // ISO-8601

    @SerializedName("inspectionTemplateId")
    private String inspectionTemplateId;

    @SerializedName("notes")
    private String notes;

    @SerializedName("assignments")
    private List<AssignmentRequest> assignments;

    public CreateInspectionRequest() {}

    public CreateInspectionRequest(String buildingId, String type, String scheduledDate,
                                   String inspectionTemplateId, String notes,
                                   List<AssignmentRequest> assignments) {
        this.buildingId = buildingId;
        this.type = type;
        this.scheduledDate = scheduledDate;
        this.inspectionTemplateId = inspectionTemplateId;
        this.notes = notes;
        this.assignments = assignments;
    }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getInspectionTemplateId() { return inspectionTemplateId; }
    public void setInspectionTemplateId(String inspectionTemplateId) { this.inspectionTemplateId = inspectionTemplateId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<AssignmentRequest> getAssignments() { return assignments; }
    public void setAssignments(List<AssignmentRequest> assignments) { this.assignments = assignments; }
}
