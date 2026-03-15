package com.inspections.controller;

import com.inspections.dto.CreateInspectionRequest;
import com.inspections.dto.CreateInspectionResponse;
import com.inspections.dto.InspectionListResponse;
import com.inspections.service.InspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de inspecciones.
 *
 * GET /api/inspections – Lista todas las inspecciones
 * POST /api/inspections – Crea una inspección building-wide
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

    @PostMapping
    @Operation(summary = "Crear inspección",
               description = "Crea una inspección building-wide con snapshot de locations, zones, devices y tests")
    public ResponseEntity<CreateInspectionResponse> createInspection(
            @Valid @RequestBody CreateInspectionRequest request) {
        CreateInspectionResponse created = inspectionService.createInspection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
