package com.inspections.dto;

import java.time.Instant;

/**
 * DTO de respuesta para una Observation.
 */
public class ObservationResponse {

    public String id;
    public String testStepId;
    public String inspectionId;
    public String name;
    public String type;
    public String description;
    public String deficiencyTypeId;
    public String mediaId;
    public Instant createdAt;
    public Instant updatedAt;
}
