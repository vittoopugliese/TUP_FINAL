package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Device.
 * Equipo físico que requiere tests. Multiples categorías especializadas
 * (FA_FIELD_DEVICE, FACP_DEVICE, JOCKEY_PUMP, FIRE_PUMP, etc.)
 */
@Entity
@Table(name = "devices", indexes = {
        @Index(name = "idx_device_zone", columnList = "zoneId"),
        @Index(name = "idx_device_location", columnList = "locationId"),
        @Index(name = "idx_device_building", columnList = "buildingId"),
        @Index(name = "idx_device_category", columnList = "deviceCategory")
})
public class Device {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    private String zoneId;
    private String locationId;
    private String buildingId;
    private String deviceTypeId;

    /**
     * Categoría: FA_FIELD_DEVICE, FACP_DEVICE, JOCKEY_PUMP, FIRE_PUMP,
     * PUMP_CONTROLLER, SPRINKLER_DEVICE, …
     */
    private String deviceCategory;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer deviceSerialNumber;
    private Instant installationDate;
    private Instant expirationDate;
    private boolean enabled = true;

    /** IDs de atributos adicionales (JSON array). */
    @Column(columnDefinition = "TEXT")
    private String attributeIds;

    private Instant createdAt;
    private Instant updatedAt;

    public Device() {
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(String v) {
        this.deviceTypeId = v;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String v) {
        this.deviceCategory = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(Integer v) {
        this.deviceSerialNumber = v;
    }

    public Instant getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(Instant v) {
        this.installationDate = v;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Instant v) {
        this.expirationDate = v;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAttributeIds() {
        return attributeIds;
    }

    public void setAttributeIds(String v) {
        this.attributeIds = v;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
