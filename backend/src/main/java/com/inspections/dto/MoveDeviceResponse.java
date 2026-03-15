package com.inspections.dto;

import java.time.Instant;

/**
 * Response DTO para la operación de mover dispositivo entre zonas.
 */
public class MoveDeviceResponse {

    private String deviceId;
    private String oldZoneId;
    private String newZoneId;
    private String locationId;
    private Instant updatedAt;

    public MoveDeviceResponse() {}

    public MoveDeviceResponse(String deviceId, String oldZoneId, String newZoneId,
                              String locationId, Instant updatedAt) {
        this.deviceId = deviceId;
        this.oldZoneId = oldZoneId;
        this.newZoneId = newZoneId;
        this.locationId = locationId;
        this.updatedAt = updatedAt;
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getOldZoneId() { return oldZoneId; }
    public void setOldZoneId(String oldZoneId) { this.oldZoneId = oldZoneId; }

    public String getNewZoneId() { return newZoneId; }
    public void setNewZoneId(String newZoneId) { this.newZoneId = newZoneId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
