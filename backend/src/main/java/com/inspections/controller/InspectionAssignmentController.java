package com.inspections.controller;

import com.inspections.dto.AssignmentRequest;
import com.inspections.dto.AssignmentResponse;
import com.inspections.entity.InspectionAssignment;
import com.inspections.service.InspectionAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints para asignaciones de inspectores y operadores a inspecciones.
 */
@RestController
@RequestMapping("/api/inspections")
@Tag(name = "Inspection Assignments", description = "Asignar inspectores y operadores a inspecciones")
public class InspectionAssignmentController {

    private final InspectionAssignmentService assignmentService;

    public InspectionAssignmentController(InspectionAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{inspectionId}/assignments")
    @Operation(summary = "Listar asignaciones", description = "Retorna todas las asignaciones de una inspeccion")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(@PathVariable String inspectionId) {
        List<InspectionAssignment> assignments = assignmentService.getAssignments(inspectionId);
        List<AssignmentResponse> response = assignments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{inspectionId}/assignments")
    @Operation(summary = "Agregar asignacion", description = "Agrega un inspector o operador a la inspeccion")
    public ResponseEntity<?> addAssignment(@PathVariable String inspectionId, @RequestBody AssignmentRequest request) {
        try {
            InspectionAssignment assignment = assignmentService.addAssignment(
                    inspectionId,
                    request.getUserEmail(),
                    request.getRole()
            );
            return ResponseEntity.ok(toResponse(assignment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{inspectionId}/assignments/{email}")
    @Operation(summary = "Remover asignacion", description = "Remueve un usuario de la inspeccion. No se puede remover al unico inspector.")
    public ResponseEntity<?> removeAssignment(@PathVariable String inspectionId, @PathVariable String email) {
        try {
            String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);
            assignmentService.removeAssignment(inspectionId, decodedEmail);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private AssignmentResponse toResponse(InspectionAssignment a) {
        return new AssignmentResponse(
                a.getId(),
                a.getInspectionId(),
                a.getUserEmail(),
                a.getRole(),
                a.getCreatedAt()
        );
    }
}
