package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO para un tipo de dispositivo del catálogo.
 */
public class DeviceTypeResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("enabled")
    private boolean enabled = true;

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
