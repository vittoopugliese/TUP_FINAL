package com.inspections.dto.report;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO interno para locations en el reporte PDF.
 */
public class ReportLocationData {

    private String id;
    private String name;
    private String details;
    private List<ReportZoneData> zones = new ArrayList<>();

    public ReportLocationData() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public List<ReportZoneData> getZones() { return zones; }
    public void setZones(List<ReportZoneData> zones) { this.zones = zones != null ? zones : new ArrayList<>(); }
}
