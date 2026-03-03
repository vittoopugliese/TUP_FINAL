package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Photo (Media).
 * Imagen subida como evidencia. Las Observations referencian Photo via mediaId.
 */
@Entity
@Table(name = "photos", indexes = {
    @Index(name = "idx_photo_step",   columnList = "stepId"),
    @Index(name = "idx_photo_device", columnList = "deviceId")
})
public class Photo {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** URL de descarga. */
    private String mediaUrl;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Detalles del archivo como JSON. */
    @Column(columnDefinition = "TEXT")
    private String fileDetailsJson;

    /** Ruta local cuando se almacena offline antes de sincronizar. */
    private String localPath;

    private Instant timestamp;

    /** ID del inspector que capturó la foto. */
    private String inspectorId;

    /** FK hacia Step. */
    private String stepId;

    /** FK hacia Device. */
    private String deviceId;

    private Instant createdAt;

    public Photo() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileDetailsJson() { return fileDetailsJson; }
    public void setFileDetailsJson(String v) { this.fileDetailsJson = v; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getInspectorId() { return inspectorId; }
    public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
