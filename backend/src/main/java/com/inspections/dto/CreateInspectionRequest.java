package com.inspections.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/**
 * Request DTO para crear una inspección desde un building.
 * La inspección es building-wide; locationId queda null.
 * <p>
 * {@code assignments}: opcional si el creador es INSPECTOR (el backend asigna al creador como único inspector
 * y solo usa del body las filas con rol OPERATOR). Si el creador es ADMIN, debe incluir al menos una asignación
 * con rol INSPECTOR (exactamente una) y las de OPERATOR que correspondan.
 */
public class CreateInspectionRequest {

    @NotBlank(message = "buildingId is required")
    private String buildingId;

    @NotBlank(message = "type is required")
    private String type;

    @NotNull(message = "scheduledDate is required")
    private Instant scheduledDate;

    @NotBlank(message = "inspectionTemplateId is required")
    private String inspectionTemplateId;

    private String notes;

    @Valid
    private List<AssignmentRequest> assignments;

    public CreateInspectionRequest() {}

    public CreateInspectionRequest(String buildingId, String type, Instant scheduledDate,
                                   String inspectionTemplateId, String notes,
                                   List<AssignmentRequest> assignments) {
        this.buildingId = buildingId;
        this.type = type;
        this.scheduledDate = scheduledDate;
        this.inspectionTemplateId = inspectionTemplateId;
        this.notes = notes;
        this.assignments = assignments;
    }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Instant getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Instant scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getInspectionTemplateId() { return inspectionTemplateId; }
    public void setInspectionTemplateId(String inspectionTemplateId) { this.inspectionTemplateId = inspectionTemplateId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<AssignmentRequest> getAssignments() { return assignments; }
    public void setAssignments(List<AssignmentRequest> assignments) { this.assignments = assignments; }
}
