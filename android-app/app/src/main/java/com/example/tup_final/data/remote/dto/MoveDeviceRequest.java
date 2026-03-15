package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO para mover un dispositivo a otra zona.
 */
public class MoveDeviceRequest {

    @SerializedName("targetZoneId")
    private String targetZoneId;

    public MoveDeviceRequest() {}

    public MoveDeviceRequest(String targetZoneId) {
        this.targetZoneId = targetZoneId;
    }

    public String getTargetZoneId() {
        return targetZoneId;
    }

    public void setTargetZoneId(String targetZoneId) {
        this.targetZoneId = targetZoneId;
    }
}
