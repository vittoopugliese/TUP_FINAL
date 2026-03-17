package com.inspections.service;

import com.inspections.dto.CreateObservationRequest;
import com.inspections.dto.ObservationResponse;
import com.inspections.entity.Observation;
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

    private final ObservationRepository observationRepository;
    private final StepRepository stepRepository;

    public ObservationService(ObservationRepository observationRepository,
                              StepRepository stepRepository) {
        this.observationRepository = observationRepository;
        this.stepRepository = stepRepository;
    }

    @Transactional
    public ObservationResponse createObservation(String stepId, CreateObservationRequest request) {
        stepRepository.findById(stepId)
                .orElseThrow(() -> new IllegalArgumentException("Step no encontrado: " + stepId));

        if ("DEFICIENCIES".equals(request.getType())
                && (request.getMediaId() == null || request.getMediaId().isBlank())) {
            throw new IllegalArgumentException("Una deficiencia requiere foto adjunta.");
        }

        Observation obs = new Observation();
        obs.setId(UUID.randomUUID().toString());
        obs.setTestStepId(stepId);
        obs.setInspectionId(request.getInspectionId());
        obs.setType(request.getType());
        obs.setDescription(request.getDescription());
        obs.setDeficiencyTypeId(request.getDeficiencyTypeId());
        obs.setMediaId(request.getMediaId());
        obs.setName(labelForType(request.getType()));
        obs.setCreatedAt(Instant.now());
        obs.setUpdatedAt(Instant.now());

        return toResponse(observationRepository.save(obs));
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
        return "DEFICIENCIES".equals(type) ? "Deficiencia" : "Observación";
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
