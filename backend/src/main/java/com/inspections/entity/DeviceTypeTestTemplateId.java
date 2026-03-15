package com.inspections.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for DeviceTypeTestTemplate.
 */
public class DeviceTypeTestTemplateId implements Serializable {

    private String deviceTypeId;
    private String testTemplateId;

    public DeviceTypeTestTemplateId() {}

    public DeviceTypeTestTemplateId(String deviceTypeId, String testTemplateId) {
        this.deviceTypeId = deviceTypeId;
        this.testTemplateId = testTemplateId;
    }

    public String getDeviceTypeId() { return deviceTypeId; }
    public void setDeviceTypeId(String deviceTypeId) { this.deviceTypeId = deviceTypeId; }

    public String getTestTemplateId() { return testTemplateId; }
    public void setTestTemplateId(String testTemplateId) { this.testTemplateId = testTemplateId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceTypeTestTemplateId that = (DeviceTypeTestTemplateId) o;
        return Objects.equals(deviceTypeId, that.deviceTypeId)
                && Objects.equals(testTemplateId, that.testTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceTypeId, testTemplateId);
    }
}
