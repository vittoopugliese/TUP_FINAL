package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para Step en respuestas API.
 */
public class StepResponse {
    @SerializedName("id")
    public String id;
    @SerializedName("testId")
    public String testId;
    @SerializedName("name")
    public String name;
    @SerializedName("testStepType")
    public String testStepType;
    @SerializedName("applicable")
    public boolean applicable;
    @SerializedName("status")
    public String status;
    @SerializedName("description")
    public String description;
    @SerializedName("valueJson")
    public String valueJson;
    @SerializedName("minValue")
    public Double minValue;
    @SerializedName("maxValue")
    public Double maxValue;
    @SerializedName("createdAt")
    public String createdAt;
    @SerializedName("updatedAt")
    public String updatedAt;
}
