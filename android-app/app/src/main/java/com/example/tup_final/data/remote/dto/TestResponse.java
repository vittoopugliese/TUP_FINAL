package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para un test en la respuesta jerárquica de zonas/devices/tests.
 */
public class TestResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("inspectionId")
    private String inspectionId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

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
