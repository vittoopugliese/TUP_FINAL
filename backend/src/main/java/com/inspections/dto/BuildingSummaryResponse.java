package com.inspections.dto;

/**
 * DTO con resumen de estructura de un building (conteos para herencia).
 */
public class BuildingSummaryResponse {

    private int locationsCount;
    private int zonesCount;
    private int devicesCount;
    private int testsCount; // estimado: tests que se crearían por device_type_test_templates

    public BuildingSummaryResponse() {}

    public BuildingSummaryResponse(int locationsCount, int zonesCount, int devicesCount, int testsCount) {
        this.locationsCount = locationsCount;
        this.zonesCount = zonesCount;
        this.devicesCount = devicesCount;
        this.testsCount = testsCount;
    }

    public int getLocationsCount() { return locationsCount; }
    public void setLocationsCount(int locationsCount) { this.locationsCount = locationsCount; }

    public int getZonesCount() { return zonesCount; }
    public void setZonesCount(int zonesCount) { this.zonesCount = zonesCount; }

    public int getDevicesCount() { return devicesCount; }
    public void setDevicesCount(int devicesCount) { this.devicesCount = devicesCount; }

    public int getTestsCount() { return testsCount; }
    public void setTestsCount(int testsCount) { this.testsCount = testsCount; }
}
