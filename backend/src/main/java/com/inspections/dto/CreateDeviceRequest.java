package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO para crear un dispositivo en una zona.
 * zoneId y locationId se obtienen del path.
 * name y deviceTypeId son requeridos; deviceCategory se deriva del tipo en backend.
 * deviceCategory (opcional): si se envía, se usa; si no, se deriva de deviceTypeId.
 * inspectionId (opcional): si se envía, se crean tests heredados para esa inspección.
 * Rollout: clientes nuevos envían inspectionId para herencia; legacy sin inspectionId crea solo device.
 */
public class CreateDeviceRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "deviceTypeId is required")
    private String deviceTypeId;

    /** Legacy: si se envía, se usa; si no, se deriva de deviceTypeId. */
    private String deviceCategory;

    /** Si se envía, se crean tests heredados para esta inspección. Opcional para compatibilidad. */
    private String inspectionId;

    private String description;
    private Integer serialNumber;
    private Boolean enabled = true;

    public CreateDeviceRequest() {}

    public CreateDeviceRequest(String name, String deviceTypeId, String deviceCategory,
                               String inspectionId, String description, Integer serialNumber, Boolean enabled) {
        this.name = name;
        this.deviceTypeId = deviceTypeId;
        this.deviceCategory = deviceCategory;
        this.inspectionId = inspectionId;
        this.description = description;
        this.serialNumber = serialNumber;
        this.enabled = enabled != null ? enabled : true;
    }

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceTypeId() { return deviceTypeId; }
    public void setDeviceTypeId(String deviceTypeId) { this.deviceTypeId = deviceTypeId; }

    public String getDeviceCategory() { return deviceCategory; }
    public void setDeviceCategory(String deviceCategory) { this.deviceCategory = deviceCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSerialNumber() { return serialNumber; }
    public void setSerialNumber(Integer serialNumber) { this.serialNumber = serialNumber; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled != null ? enabled : true; }
}
