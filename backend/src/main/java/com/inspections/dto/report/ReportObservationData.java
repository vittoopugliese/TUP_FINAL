package com.inspections.dto.report;

import java.time.Instant;

/**
 * DTO interno para observaciones en el reporte PDF.
 */
public class ReportObservationData {

    private String id;
    private String name;
    private String type;
    private String description;
    private String deficiencyTypeId;
    private String deficiencyTypeName;
    private String mediaId;
    private String mediaUrl;
    private String photoMetadata;
    private Instant createdAt;

    public ReportObservationData() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDeficiencyTypeId() { return deficiencyTypeId; }
    public void setDeficiencyTypeId(String deficiencyTypeId) { this.deficiencyTypeId = deficiencyTypeId; }

    public String getDeficiencyTypeName() { return deficiencyTypeName; }
    public void setDeficiencyTypeName(String deficiencyTypeName) { this.deficiencyTypeName = deficiencyTypeName; }

    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getPhotoMetadata() { return photoMetadata; }
    public void setPhotoMetadata(String photoMetadata) { this.photoMetadata = photoMetadata; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
