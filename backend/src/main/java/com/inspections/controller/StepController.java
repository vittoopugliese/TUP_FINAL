package com.inspections.controller;

import com.inspections.dto.StepResponse;
import com.inspections.dto.UpdateStepRequest;
import com.inspections.service.StepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API para steps de tests.
 *
 * GET /api/tests/{testId}/steps - Lista steps del test
 * PATCH /api/steps/{stepId} - Actualiza step y recalcula estado del test
 */
@RestController
@Tag(name = "Steps", description = "Pasos de verificación de tests")
public class StepController {

    private final StepService stepService;

    public StepController(StepService stepService) {
        this.stepService = stepService;
    }

    @GetMapping("/api/tests/{testId}/steps")
    @Operation(summary = "Listar steps de un test",
               description = "Retorna los steps del test ordenados por fecha de creación.")
    public ResponseEntity<List<StepResponse>> getStepsByTestId(@PathVariable String testId) {
        List<StepResponse> steps = stepService.getStepsByTestId(testId);
        return ResponseEntity.ok(steps);
    }

    @PatchMapping("/api/steps/{stepId}")
    @Operation(summary = "Actualizar step",
               description = "Actualiza valueJson y/o applicable. Recalcula automáticamente el estado del test.")
    public ResponseEntity<StepResponse> updateStep(
            @PathVariable String stepId,
            @Valid @RequestBody UpdateStepRequest request) {
        StepResponse updated = stepService.updateStep(stepId, request);
        return ResponseEntity.ok(updated);
    }
}
