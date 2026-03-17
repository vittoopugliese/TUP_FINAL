package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request para actualizar un Step (PATCH /api/steps/{stepId}).
 */
public class UpdateStepRequest {
    @SerializedName("valueJson")
    public String valueJson;
    @SerializedName("applicable")
    public Boolean applicable;

    public UpdateStepRequest() {}

    public UpdateStepRequest(String valueJson, Boolean applicable) {
        this.valueJson = valueJson;
        this.applicable = applicable;
    }
}
