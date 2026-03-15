package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO para mover un dispositivo a otra zona dentro de la misma location.
 */
public class MoveDeviceRequest {

    @NotBlank(message = "targetZoneId is required")
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
