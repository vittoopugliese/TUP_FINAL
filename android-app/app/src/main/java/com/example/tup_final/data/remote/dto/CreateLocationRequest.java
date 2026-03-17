package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO para crear una ubicación.
 */
public class CreateLocationRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("details")
    private String details;

    @SerializedName("buildingId")
    private String buildingId;

    public CreateLocationRequest() {}

    public CreateLocationRequest(String name, String details, String buildingId) {
        this.name = name;
        this.details = details;
        this.buildingId = buildingId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }
}
