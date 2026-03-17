package com.inspections.service;

import com.inspections.dto.StepResponse;
import com.inspections.dto.UpdateStepRequest;
import com.inspections.entity.InspectionTest;
import com.inspections.entity.Step;
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
 * Servicio para steps y recálculo de estado del test.
 */
@Service
public class StepService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_SUCCESS_LEGACY = "SUCCESS";

    private final StepRepository stepRepository;
    private final TestRepository testRepository;

    public StepService(StepRepository stepRepository, TestRepository testRepository) {
        this.stepRepository = stepRepository;
        this.testRepository = testRepository;
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

        recalculateTestStatus(step.getTestId());
        return mapToResponse(step);
    }

    /**
     * Calcula el estado del step según valor y applicable.
     */
    private String calculateStepStatus(Step step) {
        if (!step.isApplicable()) {
            return STATUS_COMPLETED; // N/A cuenta como completado para progreso
        }
        if (step.getValueJson() == null || step.getValueJson().isBlank()) {
            return STATUS_PENDING;
        }
        // Validación básica: si tiene valor y no hay error explícito, COMPLETED
        // La validación de rangos se hace en cliente; aquí asumimos que si guardó, pasó
        return STATUS_COMPLETED;
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
