package com.inspections.controller;

import com.inspections.dto.AssignmentRequest;
import com.inspections.dto.AssignmentResponse;
import com.inspections.entity.InspectionAssignment;
import com.inspections.service.InspectionAssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Operation(summary = "Remover asignacion", description = "Solo ADMIN puede remover al inspector. INSPECTOR puede remover operadores.")
    public ResponseEntity<?> removeAssignment(@PathVariable String inspectionId, @PathVariable String email) {
        try {
            String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);
            String currentUserRole = extractRoleFromAuth();
            assignmentService.removeAssignment(inspectionId, decodedEmail, currentUserRole);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String extractRoleFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return "INSPECTOR";
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("INSPECTOR");
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
