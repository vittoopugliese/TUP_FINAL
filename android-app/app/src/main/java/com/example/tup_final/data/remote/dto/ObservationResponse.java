package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ObservationResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("testStepId")
    public String testStepId;

    @SerializedName("inspectionId")
    public String inspectionId;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public String type;

    @SerializedName("description")
    public String description;

    @SerializedName("deficiencyTypeId")
    public String deficiencyTypeId;

    @SerializedName("mediaId")
    public String mediaId;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;
}
