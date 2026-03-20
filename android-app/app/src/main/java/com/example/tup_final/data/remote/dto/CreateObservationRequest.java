package com.example.tup_final.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request para crear una observación en un step.
 * type = "REMARKS"      → Observación (foto opcional)
 * type = "DEFICIENCIES" → Deficiencia (foto obligatoria)
 */
public class CreateObservationRequest {

    @SerializedName("type")
    public String type;

    @SerializedName("description")
    public String description;

    @SerializedName("inspectionId")
    public String inspectionId;

    @SerializedName("mediaId")
    public String mediaId;

    @SerializedName("deficiencyTypeId")
    public String deficiencyTypeId;

    public CreateObservationRequest() {}

    public CreateObservationRequest(String type, String description,
                                    String inspectionId, String mediaId,
                                    String deficiencyTypeId) {
        this.type             = type;
        this.description      = description;
        this.inspectionId     = inspectionId;
        this.mediaId          = mediaId;
        this.deficiencyTypeId = deficiencyTypeId;
    }
}
