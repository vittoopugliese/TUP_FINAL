package com.inspections.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request para crear una observación en un step.
 * type=REMARKS → Observación (texto req, foto opcional)
 * type=DEFICIENCIES → Deficiencia (texto req, foto req)
 */
public class CreateObservationRequest {

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "REMARKS|DEFICIENCIES",
             message = "El tipo debe ser REMARKS o DEFICIENCIES")
    private String type;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    /** FK a la Inspection. */
    private String inspectionId;

    /** ID del media/foto adjunto (obligatorio si type=DEFICIENCIES). */
    private String mediaId;

    /** ID del tipo de deficiencia (opcional). */
    private String deficiencyTypeId;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getDeficiencyTypeId() { return deficiencyTypeId; }
    public void setDeficiencyTypeId(String deficiencyTypeId) { this.deficiencyTypeId = deficiencyTypeId; }
}
