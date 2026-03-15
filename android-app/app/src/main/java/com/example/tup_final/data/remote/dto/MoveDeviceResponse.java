package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO para la operación de mover dispositivo entre zonas.
 */
public class MoveDeviceResponse {

    @SerializedName("deviceId")
    private String deviceId;

    @SerializedName("oldZoneId")
    private String oldZoneId;

    @SerializedName("newZoneId")
    private String newZoneId;

    @SerializedName("locationId")
    private String locationId;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getOldZoneId() { return oldZoneId; }
    public void setOldZoneId(String oldZoneId) { this.oldZoneId = oldZoneId; }

    public String getNewZoneId() { return newZoneId; }
    public void setNewZoneId(String newZoneId) { this.newZoneId = newZoneId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
