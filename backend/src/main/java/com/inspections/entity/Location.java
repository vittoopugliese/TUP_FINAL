package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad JPA para Location.
 * Área física dentro de un edificio. Agrupa Zones y Devices.
 */
@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_location_building", columnList = "buildingId"),
    @Index(name = "idx_location_name",     columnList = "name")
})
public class Location {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    private String buildingId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String details;

    private Instant createdAt;
    private Instant updatedAt;

    public Location() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
