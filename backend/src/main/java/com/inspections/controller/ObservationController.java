package com.inspections.controller;

import com.inspections.dto.CreateObservationRequest;
import com.inspections.dto.ObservationResponse;
import com.inspections.service.ObservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API de observaciones y deficiencias sobre pasos de tests.
 *
 * POST /api/steps/{stepId}/observations  – Crear observación o deficiencia
 * GET  /api/steps/{stepId}/observations  – Listar observaciones del step
 * GET  /api/inspections/{id}/observations – Listar todas las obs de una inspección
 */
@RestController
@Tag(name = "Observations", description = "Observaciones y deficiencias adjuntas a pasos de tests")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @PostMapping("/api/steps/{stepId}/observations")
    @Operation(
        summary = "Crear observación o deficiencia en un step",
        description = "type=REMARKS: texto obligatorio, foto opcional. " +
                      "type=DEFICIENCIES: texto y foto obligatorios."
    )
    public ResponseEntity<ObservationResponse> createObservation(
            @PathVariable String stepId,
            @Valid @RequestBody CreateObservationRequest request) {
        ObservationResponse response = observationService.createObservation(stepId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/steps/{stepId}/observations")
    @Operation(summary = "Listar observaciones de un step")
    public ResponseEntity<List<ObservationResponse>> getObservationsByStep(
            @PathVariable String stepId) {
        return ResponseEntity.ok(observationService.getObservationsByStep(stepId));
    }

    @GetMapping("/api/inspections/{inspectionId}/observations")
    @Operation(summary = "Listar todas las observaciones de una inspección")
    public ResponseEntity<List<ObservationResponse>> getObservationsByInspection(
            @PathVariable String inspectionId) {
        return ResponseEntity.ok(observationService.getObservationsByInspection(inspectionId));
    }
}
