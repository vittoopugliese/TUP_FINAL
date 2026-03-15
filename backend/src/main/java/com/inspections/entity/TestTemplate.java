package com.inspections.entity;

import jakarta.persistence.*;

/**
 * Entidad JPA para el catálogo global de templates de test.
 * Read-only, seeded en data.sql.
 */
@Entity
@Table(name = "test_templates", indexes = {
    @Index(name = "idx_test_template_code", columnList = "code", unique = true),
    @Index(name = "idx_test_template_enabled", columnList = "enabled")
})
public class TestTemplate {

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

    public TestTemplate() {}

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
