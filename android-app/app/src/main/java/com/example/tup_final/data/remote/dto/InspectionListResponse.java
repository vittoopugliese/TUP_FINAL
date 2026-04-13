package com.example.tup_final.data.remote.dto;

/**
 * DTO para la lista de inspecciones.
 * Coincide con InspectionListResponse del backend.
 */
public class InspectionListResponse {

    private String id;
    private String buildingId;
    private String buildingName;
    private String locationId;
    private String status;
    private String scheduledDate;
    private String type;
    private String signer;
    private boolean signed;
    private String signDate;
    private String result;
    private String notes;
    private String createdByEmail;

    public InspectionListResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSigner() { return signer; }
    public void setSigner(String signer) { this.signer = signer; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public String getSignDate() { return signDate; }
    public void setSignDate(String signDate) { this.signDate = signDate; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedByEmail() { return createdByEmail; }
    public void setCreatedByEmail(String createdByEmail) { this.createdByEmail = createdByEmail; }
}
