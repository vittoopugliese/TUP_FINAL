package com.inspections.service;

import com.inspections.dto.CreateObservationRequest;
import com.inspections.dto.ObservationResponse;
import com.inspections.entity.Observation;
import com.inspections.entity.Step;
import com.inspections.repository.ObservationRepository;
import com.inspections.repository.StepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para crear y consultar observaciones en steps.
 * Tipos:
 *   REMARKS     → Observación (texto requerido, foto opcional)
 *   DEFICIENCIES → Deficiencia (texto requerido, foto requerida, step pasa a FAILED)
 */
@Service
public class ObservationService {

    private static final String TYPE_DEFICIENCIES = "DEFICIENCIES";
    private static final String TYPE_DEFICIENCY_LEGACY = "DEFICIENCY";

    private final ObservationRepository observationRepository;
    private final StepRepository stepRepository;
    private final StepService stepService;

    public ObservationService(ObservationRepository observationRepository,
                              StepRepository stepRepository,
                              StepService stepService) {
        this.observationRepository = observationRepository;
        this.stepRepository = stepRepository;
        this.stepService = stepService;
    }

    @Transactional
    public ObservationResponse createObservation(String stepId, CreateObservationRequest request) {
        Step step = stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step no encontrado: " + stepId));

        String type = normalizeType(request.getType());
        if (TYPE_DEFICIENCIES.equals(type)
                && (request.getMediaId() == null || request.getMediaId().isBlank())) {
            throw new IllegalArgumentException("Una deficiencia requiere foto adjunta.");
        }

        Observation obs = new Observation();
        obs.setId(UUID.randomUUID().toString());
        obs.setTestStepId(stepId);
        obs.setInspectionId(request.getInspectionId());
        obs.setType(type);
        obs.setDescription(request.getDescription());
        obs.setDeficiencyTypeId(request.getDeficiencyTypeId());
        obs.setMediaId(request.getMediaId());
        obs.setName(labelForType(type));
        obs.setCreatedAt(Instant.now());
        obs.setUpdatedAt(Instant.now());

        observationRepository.save(obs);

        if (TYPE_DEFICIENCIES.equals(type)) {
            step.setStatus("FAILED");
            step.setUpdatedAt(Instant.now());
            stepRepository.save(step);
            stepService.recalculateTestStatus(step.getTestId());
        }

        return toResponse(obs);
    }

    private String normalizeType(String type) {
        if (type == null) return "REMARKS";
        if (TYPE_DEFICIENCY_LEGACY.equalsIgnoreCase(type)) return TYPE_DEFICIENCIES;
        return type;
    }

    public List<ObservationResponse> getObservationsByStep(String stepId) {
        return observationRepository.findByTestStepId(stepId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ObservationResponse> getObservationsByInspection(String inspectionId) {
        return observationRepository.findByInspectionId(inspectionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String labelForType(String type) {
        return TYPE_DEFICIENCIES.equals(type) ? "Deficiencia" : "Observación";
    }

    private ObservationResponse toResponse(Observation obs) {
        ObservationResponse r = new ObservationResponse();
        r.id = obs.getId();
        r.testStepId = obs.getTestStepId();
        r.inspectionId = obs.getInspectionId();
        r.name = obs.getName();
        r.type = obs.getType();
        r.description = obs.getDescription();
        r.deficiencyTypeId = obs.getDeficiencyTypeId();
        r.mediaId = obs.getMediaId();
        r.createdAt = obs.getCreatedAt();
        r.updatedAt = obs.getUpdatedAt();
        return r;
    }
}
