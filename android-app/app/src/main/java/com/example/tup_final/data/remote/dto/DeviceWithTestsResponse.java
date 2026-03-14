package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO para un device con sus tests en la respuesta jerárquica.
 */
public class DeviceWithTestsResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("zoneId")
    private String zoneId;

    @SerializedName("locationId")
    private String locationId;

    @SerializedName("name")
    private String name;

    @SerializedName("deviceCategory")
    private String deviceCategory;

    @SerializedName("deviceSerialNumber")
    private Integer deviceSerialNumber;

    @SerializedName("enabled")
    private boolean enabled = true;

    @SerializedName("tests")
    private List<TestResponse> tests;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceCategory() { return deviceCategory; }
    public void setDeviceCategory(String deviceCategory) { this.deviceCategory = deviceCategory; }

    public Integer getDeviceSerialNumber() { return deviceSerialNumber; }
    public void setDeviceSerialNumber(Integer deviceSerialNumber) { this.deviceSerialNumber = deviceSerialNumber; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<TestResponse> getTests() { return tests; }
    public void setTests(List<TestResponse> tests) { this.tests = tests; }
}
