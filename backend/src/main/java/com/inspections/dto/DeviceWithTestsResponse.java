package com.inspections.dto;

import java.util.List;

public class DeviceWithTestsResponse {
    private String id;
    private String zoneId;
    private String locationId;
    private String name;
    private String deviceCategory;
    private Integer deviceSerialNumber;
    private boolean enabled;
    private List<TestResponse> tests;

    public DeviceWithTestsResponse() {}

    public DeviceWithTestsResponse(String id, String zoneId, String locationId,
                                   String name, String deviceCategory,
                                   Integer deviceSerialNumber, boolean enabled,
                                   List<TestResponse> tests) {
        this.id = id;
        this.zoneId = zoneId;
        this.locationId = locationId;
        this.name = name;
        this.deviceCategory = deviceCategory;
        this.deviceSerialNumber = deviceSerialNumber;
        this.enabled = enabled;
        this.tests = tests;
    }

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
