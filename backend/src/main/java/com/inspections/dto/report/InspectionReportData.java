package com.inspections.dto.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO raíz con todos los datos para generar el reporte PDF de una inspección.
 */
public class InspectionReportData {

    private String inspectionId;
    private String buildingId;
    private String buildingName;
    private String type;
    private String status;
    private String result;
    private String notes;
    private Instant scheduledDate;
    private Instant startedAt;
    private Instant signDate;
    private String signer;
    private boolean signed;
    private List<String> inspectorEmails = new ArrayList<>();
    private List<String> operatorEmails = new ArrayList<>();
    private List<ReportLocationData> locations = new ArrayList<>();

    /** Resumen ejecutivo */
    private int locationCount;
    private int zoneCount;
    private int deviceCount;
    private int testCount;
    private int stepCount;
    private int observationCount;
    private int deficiencyCount;
    private int photoCount;

    public InspectionReportData() {}

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getSignDate() { return signDate; }
    public void setSignDate(Instant signDate) { this.signDate = signDate; }

    public String getSigner() { return signer; }
    public void setSigner(String signer) { this.signer = signer; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public List<String> getInspectorEmails() { return inspectorEmails; }
    public void setInspectorEmails(List<String> inspectorEmails) { this.inspectorEmails = inspectorEmails != null ? inspectorEmails : new ArrayList<>(); }

    public List<String> getOperatorEmails() { return operatorEmails; }
    public void setOperatorEmails(List<String> operatorEmails) { this.operatorEmails = operatorEmails != null ? operatorEmails : new ArrayList<>(); }

    public List<ReportLocationData> getLocations() { return locations; }
    public void setLocations(List<ReportLocationData> locations) { this.locations = locations != null ? locations : new ArrayList<>(); }

    public int getLocationCount() { return locationCount; }
    public void setLocationCount(int locationCount) { this.locationCount = locationCount; }

    public int getZoneCount() { return zoneCount; }
    public void setZoneCount(int zoneCount) { this.zoneCount = zoneCount; }

    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }

    public int getTestCount() { return testCount; }
    public void setTestCount(int testCount) { this.testCount = testCount; }

    public int getStepCount() { return stepCount; }
    public void setStepCount(int stepCount) { this.stepCount = stepCount; }

    public int getObservationCount() { return observationCount; }
    public void setObservationCount(int observationCount) { this.observationCount = observationCount; }

    public int getDeficiencyCount() { return deficiencyCount; }
    public void setDeficiencyCount(int deficiencyCount) { this.deficiencyCount = deficiencyCount; }

    public int getPhotoCount() { return photoCount; }
    public void setPhotoCount(int photoCount) { this.photoCount = photoCount; }
}
