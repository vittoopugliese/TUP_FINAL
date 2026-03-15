package com.inspections.entity;

import jakarta.persistence.*;

/**
 * Mapeo device_type -> test_template para herencia de tests.
 * Define qué tests hereda cada tipo de dispositivo al crearse.
 */
@Entity
@Table(name = "device_type_test_templates", indexes = {
    @Index(name = "idx_dtt_device_type", columnList = "device_type_id"),
    @Index(name = "idx_dtt_test_template", columnList = "test_template_id")
})
@IdClass(DeviceTypeTestTemplateId.class)
public class DeviceTypeTestTemplate {

    @Id
    @Column(name = "device_type_id", nullable = false, length = 36)
    private String deviceTypeId;

    @Id
    @Column(name = "test_template_id", nullable = false, length = 36)
    private String testTemplateId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public DeviceTypeTestTemplate() {}

    public String getDeviceTypeId() { return deviceTypeId; }
    public void setDeviceTypeId(String deviceTypeId) { this.deviceTypeId = deviceTypeId; }

    public String getTestTemplateId() { return testTemplateId; }
    public void setTestTemplateId(String testTemplateId) { this.testTemplateId = testTemplateId; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
