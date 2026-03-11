package com.inspections.controller;

import com.inspections.dto.InspectionListResponse;
import com.inspections.service.InspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de inspecciones.
 *
 * GET /api/inspections – Lista todas las inspecciones
 */
@RestController
@RequestMapping("/api/inspections")
@Tag(name = "Inspections", description = "Operaciones de inspecciones")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    @Operation(summary = "Listar inspecciones",
               description = "Retorna todas las inspecciones con datos para las tarjetas")
    public ResponseEntity<List<InspectionListResponse>> getInspections() {
        List<InspectionListResponse> inspections = inspectionService.getAllInspections();
        return ResponseEntity.ok(inspections);
    }
}
