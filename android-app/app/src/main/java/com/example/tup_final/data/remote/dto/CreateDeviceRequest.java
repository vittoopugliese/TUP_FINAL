package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO para crear un dispositivo en una zona.
 * zoneId y locationId vienen del path.
 * deviceTypeId es requerido; deviceCategory se deriva en backend.
 */
public class CreateDeviceRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("deviceTypeId")
    private String deviceTypeId;

    @SerializedName("description")
    private String description;

    @SerializedName("serialNumber")
    private Integer serialNumber;

    @SerializedName("enabled")
    private Boolean enabled = true;

    public CreateDeviceRequest() {}

    public CreateDeviceRequest(String name, String deviceTypeId, String description,
                               Integer serialNumber, Boolean enabled) {
        this.name = name;
        this.deviceTypeId = deviceTypeId;
        this.description = description;
        this.serialNumber = serialNumber;
        this.enabled = enabled != null ? enabled : true;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceTypeId() { return deviceTypeId; }
    public void setDeviceTypeId(String deviceTypeId) { this.deviceTypeId = deviceTypeId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSerialNumber() { return serialNumber; }
    public void setSerialNumber(Integer serialNumber) { this.serialNumber = serialNumber; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled != null ? enabled : true; }
}
