package com.inspections.service;

import com.inspections.dto.InspectionListResponse;
import com.inspections.entity.Inspection;
import com.inspections.repository.InspectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones de inspecciones.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;

    public InspectionService(InspectionRepository inspectionRepository) {
        this.inspectionRepository = inspectionRepository;
    }

    /**
     * Obtiene todas las inspecciones para la lista.
     *
     * @return Lista de InspectionListResponse
     */
    public List<InspectionListResponse> getAllInspections() {
        return inspectionRepository.findAll().stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    private InspectionListResponse mapToListResponse(Inspection inspection) {
        return new InspectionListResponse(
                inspection.getId(),
                inspection.getBuildingId(),
                inspection.getStatus(),
                inspection.getScheduledDate(),
                inspection.getType()
        );
    }
}
