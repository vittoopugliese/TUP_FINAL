package com.inspections.dto;

import java.time.Instant;

/**
 * DTO para la lista de inspecciones.
 * Contiene los campos necesarios para mostrar las tarjetas en la app.
 */
public class InspectionListResponse {

    private String id;
    private String buildingId;
    private String buildingName;
    private String locationId;
    private String status;
    private Instant scheduledDate;
    private String type;
    private String result;
    private String signer;
    private boolean signed;
    private Instant signDate;

    public InspectionListResponse() {}

    public InspectionListResponse(String id, String buildingId, String buildingName, String locationId,
                                   String status, Instant scheduledDate, String type) {
        this.id = id;
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.locationId = locationId;
        this.status = status;
        this.scheduledDate = scheduledDate;
        this.type = type;
    }

    public InspectionListResponse(String id, String buildingId, String buildingName, String locationId,
                                   String status, Instant scheduledDate, String type, String result) {
        this(id, buildingId, buildingName, locationId, status, scheduledDate, type);
        this.result = result;
    }

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

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getSigner() { return signer; }
    public void setSigner(String signer) { this.signer = signer; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public Instant getSignDate() { return signDate; }
    public void setSignDate(Instant signDate) { this.signDate = signDate; }
}
