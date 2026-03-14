package com.inspections.dto;

public class TestResponse {
    private String id;
    private String deviceId;
    private String inspectionId;
    private String name;
    private String description;
    private String status;

    public TestResponse() {}

    public TestResponse(String id, String deviceId, String inspectionId,
                        String name, String description, String status) {
        this.id = id;
        this.deviceId = deviceId;
        this.inspectionId = inspectionId;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
