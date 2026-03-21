package com.inspections.controller;

import com.inspections.dto.DeficiencyTypeResponse;
import com.inspections.entity.DeficiencyType;
import com.inspections.repository.DeficiencyTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints read-only para el catálogo global de tipos de deficiencia.
 * GET /api/deficiency-types — listado (solo habilitados por defecto)
 * GET /api/deficiency-types/{id} — detalle por ID
 */
@RestController
@RequestMapping("/api/deficiency-types")
@Tag(name = "Deficiency Types", description = "Catálogo de tipos de deficiencia (read-only)")
public class DeficiencyTypeController {

    private final DeficiencyTypeRepository deficiencyTypeRepository;

    public DeficiencyTypeController(DeficiencyTypeRepository deficiencyTypeRepository) {
        this.deficiencyTypeRepository = deficiencyTypeRepository;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de deficiencia",
               description = "Retorna el catálogo global. Por defecto solo tipos habilitados.")
    public ResponseEntity<List<DeficiencyTypeResponse>> getDeficiencyTypes(
            @Parameter(description = "Incluir tipos deshabilitados")
            @RequestParam(required = false, defaultValue = "false") boolean includeDisabled) {

        List<DeficiencyType> types = includeDisabled
                ? deficiencyTypeRepository.findAllByOrderBySortOrderAsc()
                : deficiencyTypeRepository.findByEnabledTrueOrderBySortOrderAsc();

        List<DeficiencyTypeResponse> response = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de deficiencia por ID")
    public ResponseEntity<DeficiencyTypeResponse> getDeficiencyType(@PathVariable String id) {
        return deficiencyTypeRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private DeficiencyTypeResponse mapToResponse(DeficiencyType t) {
        return new DeficiencyTypeResponse(
                t.getId(), t.getCode(), t.getName(),
                t.getDescription(), t.getCategory(), t.isEnabled());
    }
}
