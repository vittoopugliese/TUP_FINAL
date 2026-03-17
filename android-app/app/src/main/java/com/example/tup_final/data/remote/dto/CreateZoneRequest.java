package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO para crear una zona en una ubicación.
 */
public class CreateZoneRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("details")
    private String details;

    public CreateZoneRequest() {}

    public CreateZoneRequest(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
