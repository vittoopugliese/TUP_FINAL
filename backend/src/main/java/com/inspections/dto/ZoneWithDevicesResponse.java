package com.inspections.dto;

import java.util.List;

public class ZoneWithDevicesResponse {
    private String id;
    private String locationId;
    private String name;
    private String details;
    private List<DeviceWithTestsResponse> devices;

    public ZoneWithDevicesResponse() {}

    public ZoneWithDevicesResponse(String id, String locationId, String name,
                                   String details, List<DeviceWithTestsResponse> devices) {
        this.id = id;
        this.locationId = locationId;
        this.name = name;
        this.details = details;
        this.devices = devices;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public List<DeviceWithTestsResponse> getDevices() { return devices; }
    public void setDevices(List<DeviceWithTestsResponse> devices) { this.devices = devices; }
}
