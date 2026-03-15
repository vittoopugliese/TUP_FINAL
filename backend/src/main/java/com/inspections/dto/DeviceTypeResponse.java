package com.inspections.dto;

/**
 * Response DTO para un tipo de dispositivo del catálogo.
 */
public class DeviceTypeResponse {

    private String id;
    private String code;
    private String name;
    private String category;
    private boolean enabled;

    public DeviceTypeResponse() {}

    public DeviceTypeResponse(String id, String code, String name, String category, boolean enabled) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
