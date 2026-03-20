package com.inspections.dto;

/**
 * DTO de respuesta para un tipo de deficiencia del catálogo.
 */
public class DeficiencyTypeResponse {

    public String id;
    public String code;
    public String name;
    public String description;
    public String category;
    public boolean enabled;

    public DeficiencyTypeResponse() {}

    public DeficiencyTypeResponse(String id, String code, String name,
                                   String description, String category, boolean enabled) {
        this.id          = id;
        this.code        = code;
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.enabled     = enabled;
    }
}
