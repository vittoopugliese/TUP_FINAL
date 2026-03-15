package com.inspections.controller;

import com.inspections.dto.InspectionTemplateListResponse;
import com.inspections.entity.InspectionTemplate;
import com.inspections.repository.InspectionTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints de plantillas de inspección.
 *
 * GET /api/inspection-templates – Lista plantillas disponibles
 */
@RestController
@RequestMapping("/api/inspection-templates")
@Tag(name = "Inspection Templates", description = "Catálogo de plantillas de inspección")
public class InspectionTemplateController {

    private final InspectionTemplateRepository inspectionTemplateRepository;

    public InspectionTemplateController(InspectionTemplateRepository inspectionTemplateRepository) {
        this.inspectionTemplateRepository = inspectionTemplateRepository;
    }

    @GetMapping
    @Operation(summary = "Listar plantillas",
               description = "Retorna las plantillas de inspección disponibles")
    public ResponseEntity<List<InspectionTemplateListResponse>> getTemplates() {
        List<InspectionTemplateListResponse> templates = inspectionTemplateRepository.findAll().stream()
                .filter(InspectionTemplate::isEnabled)
                .map(t -> new InspectionTemplateListResponse(t.getId(), t.getCode(), t.getName(), t.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(templates);
    }
}
