package com.inspections.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para el catálogo global de tipos de dispositivo.
 * Read-only, seeded en data.sql.
 */
@Entity
@Table(name = "device_types", indexes = {
    @Index(name = "idx_device_type_category", columnList = "category"),
    @Index(name = "idx_device_type_enabled", columnList = "enabled")
})
public class DeviceType {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    private boolean enabled = true;

    @Column(name = "sort_order")
    private int sortOrder = 0;

    public DeviceType() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
