package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para el catálogo de buildings.
 */
public class BuildingListResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("details")
    private String details;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
