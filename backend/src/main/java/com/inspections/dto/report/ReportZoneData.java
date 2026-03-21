package com.inspections.dto.report;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO interno para zones en el reporte PDF.
 */
public class ReportZoneData {

    private String id;
    private String name;
    private String details;
    private List<ReportDeviceData> devices = new ArrayList<>();

    public ReportZoneData() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public List<ReportDeviceData> getDevices() { return devices; }
    public void setDevices(List<ReportDeviceData> devices) { this.devices = devices != null ? devices : new ArrayList<>(); }
}
