package com.inspections.service;

import com.inspections.dto.StepResponse;
import com.inspections.dto.UpdateStepRequest;
import com.inspections.entity.Inspection;
import com.inspections.entity.InspectionTest;
import com.inspections.entity.Step;
import com.inspections.util.StepValueValidator;
import com.inspections.repository.InspectionRepository;
import com.inspections.repository.StepRepository;
import com.inspections.repository.TestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para steps, recálculo de estado del test y propagación
 * automática del estado hacia la inspección padre.
 *
 * Cadena de propagación: Step → Test → Inspection
 */
@Service
public class StepService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_SUCCESS_LEGACY = "SUCCESS";

    private static final String INSPECTION_IN_PROGRESS = "IN_PROGRESS";
    private static final String INSPECTION_DONE_COMPLETED = "DONE_COMPLETED";
    private static final String INSPECTION_DONE_FAILED = "DONE_FAILED";

    private final StepRepository stepRepository;
    private final TestRepository testRepository;
    private final InspectionRepository inspectionRepository;

    public StepService(StepRepository stepRepository,
                       TestRepository testRepository,
                       InspectionRepository inspectionRepository) {
        this.stepRepository = stepRepository;
        this.testRepository = testRepository;
        this.inspectionRepository = inspectionRepository;
    }

    /**
     * Lista steps de un test ordenados por createdAt.
     */
    public List<StepResponse> getStepsByTestId(String testId) {
        List<Step> steps = stepRepository.findByTestIdOrderByCreatedAtAsc(testId);
        return steps.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Actualiza un step y recalcula el estado del test.
     */
    @Transactional
    public StepResponse updateStep(String stepId, UpdateStepRequest request) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found: " + stepId));

        if (request.getValueJson() != null) {
            step.setValueJson(request.getValueJson());
        }
        if (request.getApplicable() != null) {
            step.setApplicable(request.getApplicable());
        }

        step.setUpdatedAt(Instant.now());
        step.setStatus(calculateStepStatus(step));
        stepRepository.save(step);

        String testId = step.getTestId();
        recalculateTestStatus(testId);
        recalculateInspectionStatus(testId);
        return mapToResponse(step);
    }

    /**
     * Calcula el estado del step según valor, applicable y validación por tipo.
     */
    private String calculateStepStatus(Step step) {
        if (!step.isApplicable()) {
            return STATUS_COMPLETED; // N/A cuenta como completado para progreso
        }
        if (step.getValueJson() == null || step.getValueJson().isBlank()) {
            return STATUS_PENDING;
        }
        String type = step.getTestStepType();
        if (type == null) type = "SIMPLE_VALUE";
        if ("RANGE".equals(type) && (step.getMinValue() != null || step.getMaxValue() != null)) {
            type = "NUMERIC_RANGE";
        }
        boolean valid = StepValueValidator.isValid(
                step.getValueJson(), type, step.getMinValue(), step.getMaxValue());
        return valid ? STATUS_COMPLETED : STATUS_FAILED;
    }

    /**
     * Recalcula y persiste el estado del test según sus steps.
     */
    public void recalculateTestStatus(String testId) {
        InspectionTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test not found: " + testId));

        List<Step> steps = stepRepository.findByTestIdOrderByCreatedAtAsc(testId);
        String newStatus = computeTestStatus(steps);
        test.setStatus(newStatus);
        test.setUpdatedAt(Instant.now());
        testRepository.save(test);
    }

    private String computeTestStatus(List<Step> steps) {
        boolean anyFailed = false;
        boolean anyPending = false;
        for (Step s : steps) {
            if (!s.isApplicable()) continue;
            String st = normalizeStatus(s.getStatus());
            if (STATUS_FAILED.equals(st)) anyFailed = true;
            if (STATUS_PENDING.equals(st)) anyPending = true;
        }
        if (anyFailed) return STATUS_FAILED;
        if (anyPending) return STATUS_PENDING;
        return STATUS_COMPLETED;
    }

    /**
     * Recalcula el estado de la inspección a partir de los estados de TODOS
     * sus tests. Solo actúa si la inspección está IN_PROGRESS.
     *
     * Reglas:
     *  - Si algún test está PENDING → la inspección permanece IN_PROGRESS.
     *  - Si todos los tests son COMPLETED o FAILED:
     *      - Al menos un FAILED → DONE_FAILED  (result = "FAILED")
     *      - Todos COMPLETED   → DONE_COMPLETED (result = "SUCCESS")
     */
    public void recalculateInspectionStatus(String testId) {
        InspectionTest test = testRepository.findById(testId).orElse(null);
        if (test == null) return;
        String inspectionId = test.getInspectionId();
        if (inspectionId == null) return;

        Inspection inspection = inspectionRepository.findById(inspectionId).orElse(null);
        if (inspection == null) return;

        // Solo recalcular si la inspección está en progreso
        if (!INSPECTION_IN_PROGRESS.equals(inspection.getStatus())) return;

        List<InspectionTest> allTests = testRepository.findByInspectionId(inspectionId);
        if (allTests.isEmpty()) return;

        boolean anyPending = false;
        boolean anyFailed  = false;
        for (InspectionTest t : allTests) {
            String st = normalizeStatus(t.getStatus());
            if (STATUS_PENDING.equals(st)) {
                anyPending = true;
                break; // no need to continue, inspection stays IN_PROGRESS
            }
            if (STATUS_FAILED.equals(st)) anyFailed = true;
        }

        if (anyPending) return; // remains IN_PROGRESS

        Instant now = Instant.now();
        inspection.setStatus(anyFailed ? INSPECTION_DONE_FAILED : INSPECTION_DONE_COMPLETED);
        inspection.setResult(anyFailed ? "FAILED" : "SUCCESS");
        inspection.setUpdatedAt(now);
        inspectionRepository.save(inspection);
    }

    private String normalizeStatus(String status) {
        if (STATUS_SUCCESS_LEGACY.equals(status)) return STATUS_COMPLETED;
        return status != null ? status : STATUS_PENDING;
    }

    private StepResponse mapToResponse(Step step) {
        return new StepResponse(
                step.getId(),
                step.getTestId(),
                step.getName(),
                step.getTestStepType(),
                step.isApplicable(),
                normalizeStatus(step.getStatus()),
                step.getDescription(),
                step.getValueJson(),
                step.getMinValue(),
                step.getMaxValue(),
                step.getCreatedAt() != null ? step.getCreatedAt().toString() : null,
                step.getUpdatedAt() != null ? step.getUpdatedAt().toString() : null
        );
    }
}
