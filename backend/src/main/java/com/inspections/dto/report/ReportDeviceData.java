package com.inspections.dto.report;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO interno para devices en el reporte PDF.
 */
public class ReportDeviceData {

    private String id;
    private String name;
    private String description;
    private String deviceCategory;
    private Integer deviceSerialNumber;
    private List<ReportTestData> tests = new ArrayList<>();

    public ReportDeviceData() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDeviceCategory() { return deviceCategory; }
    public void setDeviceCategory(String deviceCategory) { this.deviceCategory = deviceCategory; }

    public Integer getDeviceSerialNumber() { return deviceSerialNumber; }
    public void setDeviceSerialNumber(Integer deviceSerialNumber) { this.deviceSerialNumber = deviceSerialNumber; }

    public List<ReportTestData> getTests() { return tests; }
    public void setTests(List<ReportTestData> tests) { this.tests = tests != null ? tests : new ArrayList<>(); }
}
