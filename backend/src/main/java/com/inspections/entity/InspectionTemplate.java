package com.inspections.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para InspectionTemplate.
 * Catálogo de plantillas de inspección.
 */
@Entity
@Table(name = "inspection_templates", indexes = {
    @Index(name = "idx_inspection_template_code", columnList = "code", unique = true)
})
public class InspectionTemplate {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean enabled = true;

    @Column(name = "sort_order")
    private int sortOrder = 0;

    public InspectionTemplate() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
