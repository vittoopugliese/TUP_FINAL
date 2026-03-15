package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO con resumen de estructura de un building.
 */
public class BuildingSummaryResponse {

    @SerializedName("locationsCount")
    private int locationsCount;

    @SerializedName("zonesCount")
    private int zonesCount;

    @SerializedName("devicesCount")
    private int devicesCount;

    @SerializedName("testsCount")
    private int testsCount;

    public int getLocationsCount() { return locationsCount; }
    public void setLocationsCount(int locationsCount) { this.locationsCount = locationsCount; }

    public int getZonesCount() { return zonesCount; }
    public void setZonesCount(int zonesCount) { this.zonesCount = zonesCount; }

    public int getDevicesCount() { return devicesCount; }
    public void setDevicesCount(int devicesCount) { this.devicesCount = devicesCount; }

    public int getTestsCount() { return testsCount; }
    public void setTestsCount(int testsCount) { this.testsCount = testsCount; }
}
