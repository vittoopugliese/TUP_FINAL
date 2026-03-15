package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO para crear un dispositivo en una zona.
 * zoneId y locationId vienen del path.
 */
public class CreateDeviceRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("deviceCategory")
    private String deviceCategory;

    @SerializedName("description")
    private String description;

    @SerializedName("serialNumber")
    private Integer serialNumber;

    @SerializedName("enabled")
    private Boolean enabled = true;

    public CreateDeviceRequest() {}

    public CreateDeviceRequest(String name, String deviceCategory, String description,
                               Integer serialNumber, Boolean enabled) {
        this.name = name;
        this.deviceCategory = deviceCategory;
        this.description = description;
        this.serialNumber = serialNumber;
        this.enabled = enabled != null ? enabled : true;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceCategory() { return deviceCategory; }
    public void setDeviceCategory(String deviceCategory) { this.deviceCategory = deviceCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSerialNumber() { return serialNumber; }
    public void setSerialNumber(Integer serialNumber) { this.serialNumber = serialNumber; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled != null ? enabled : true; }
}
