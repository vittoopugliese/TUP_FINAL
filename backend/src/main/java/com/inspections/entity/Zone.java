package com.inspections.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para Zone.
 * Agrupación lógica de dispositivos dentro de una Location.
 */
@Entity
@Table(name = "zones", indexes = {
    @Index(name = "idx_zone_location", columnList = "locationId")
})
public class Zone {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    /** FK hacia Location. */
    @Column(nullable = false)
    private String locationId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String details;

    public Zone() {}

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
