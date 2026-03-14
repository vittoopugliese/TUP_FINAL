package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO para una zona con sus devices (y tests) en la respuesta jerárquica.
 */
public class ZoneWithDevicesResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("locationId")
    private String locationId;

    @SerializedName("name")
    private String name;

    @SerializedName("details")
    private String details;

    @SerializedName("devices")
    private List<DeviceWithTestsResponse> devices;

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
