package com.inspections.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspections.entity.InspectionAssignment;
import com.inspections.entity.User;
import com.inspections.repository.InspectionAssignmentRepository;
import com.inspections.repository.InspectionRepository;
import com.inspections.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para asignaciones de inspectores y operadores a inspecciones.
 * Reglas: max 1 INSPECTOR por inspeccion, operadores ilimitados.
 * Se permite remover al único inspector solo a ADMIN (para reasignar).
 */
@Service
public class InspectionAssignmentService {

    private static final String ROLE_INSPECTOR = "INSPECTOR";
    private static final String ROLE_OPERATOR = "OPERATOR";

    private final InspectionAssignmentRepository assignmentRepository;
    private final InspectionRepository inspectionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public InspectionAssignmentService(InspectionAssignmentRepository assignmentRepository,
                                       InspectionRepository inspectionRepository,
                                       UserRepository userRepository,
                                       AuditService auditService,
                                       ObjectMapper objectMapper) {
        this.assignmentRepository = assignmentRepository;
        this.inspectionRepository = inspectionRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public List<InspectionAssignment> getAssignments(String inspectionId) {
        return assignmentRepository.findByInspectionId(inspectionId);
    }

    @Transactional
    public InspectionAssignment addAssignment(String inspectionId, String userEmail, String role,
                                             String currentUserRole, String callerEmail) {
        if (inspectionId == null || userEmail == null || role == null) {
            throw new IllegalArgumentException("inspectionId, userEmail and role are required");
        }

        if (!inspectionRepository.existsById(inspectionId)) {
            throw new IllegalArgumentException("Inspection not found: " + inspectionId);
        }

        String normalizedEmail = userEmail.trim().toLowerCase();
        String normalizedRole = role.toUpperCase();
        String callerRole = (currentUserRole != null ? currentUserRole : "").toUpperCase();

        if (!ROLE_INSPECTOR.equals(normalizedRole) && !ROLE_OPERATOR.equals(normalizedRole)) {
            throw new IllegalArgumentException("Role must be INSPECTOR or OPERATOR");
        }

        if (ROLE_INSPECTOR.equals(normalizedRole) && !"ADMIN".equals(callerRole)) {
            throw new IllegalArgumentException("Solo el administrador puede asignar o reasignar al inspector");
        }

        if (assignmentRepository.existsByInspectionIdAndUserEmail(inspectionId, normalizedEmail)) {
            throw new IllegalArgumentException("This email is already assigned to this inspection");
        }

        if (ROLE_INSPECTOR.equals(normalizedRole)) {
            User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
            String accountRole = user.getRole() != null ? user.getRole().trim().toUpperCase() : "";
            if (!ROLE_INSPECTOR.equals(accountRole)) {
                throw new IllegalArgumentException("El usuario no tiene rol de Inspector");
            }
            long inspectorCount = assignmentRepository.countByInspectionIdAndRole(inspectionId, ROLE_INSPECTOR);
            if (inspectorCount >= 1) {
                throw new IllegalArgumentException("Only 1 Inspector is allowed per inspection");
            }
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no registrado: " + normalizedEmail));
        String dbRole = user.getRole() != null ? user.getRole().toUpperCase() : "";
        if (ROLE_INSPECTOR.equals(normalizedRole)) {
            if (!ROLE_INSPECTOR.equals(dbRole)) {
                throw new IllegalArgumentException(
                        "Solo usuarios con rol INSPECTOR pueden asignarse como inspector");
            }
        } else {
            if (!ROLE_INSPECTOR.equals(dbRole) && !ROLE_OPERATOR.equals(dbRole)) {
                throw new IllegalArgumentException(
                        "En operadores solo se pueden asignar usuarios con rol INSPECTOR u OPERATOR");
            }
        }

        InspectionAssignment assignment = new InspectionAssignment();
        assignment.setId(UUID.randomUUID().toString());
        assignment.setInspectionId(inspectionId);
        assignment.setUserEmail(normalizedEmail);
        assignment.setRole(normalizedRole);
        assignment.setCreatedAt(Instant.now());

        InspectionAssignment saved = assignmentRepository.save(assignment);
        String actor = callerEmail != null ? callerEmail.trim().toLowerCase() : "";
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("assigneeEmail", normalizedEmail);
        meta.put("role", normalizedRole);
        try {
            auditService.log(actor, "Inspection", inspectionId, "ASSIGNMENT_ADD",
                    objectMapper.writeValueAsString(meta));
        } catch (JsonProcessingException e) {
            auditService.log(actor, "Inspection", inspectionId, "ASSIGNMENT_ADD", null);
        }
        return saved;
    }

    @Transactional
    public void removeAssignment(String inspectionId, String userEmail, String currentUserRole,
                                 String callerEmail) {
        if (inspectionId == null || userEmail == null) {
            throw new IllegalArgumentException("inspectionId and userEmail are required");
        }

        String normalizedEmail = userEmail.trim().toLowerCase();
        String role = (currentUserRole != null ? currentUserRole : "").toUpperCase();

        if (ROLE_OPERATOR.equals(role)) {
            throw new IllegalArgumentException("Los operadores no pueden modificar asignaciones");
        }

        InspectionAssignment toRemove = assignmentRepository.findByInspectionIdAndUserEmail(inspectionId, normalizedEmail)
                .orElse(null);
        if (toRemove == null) {
            return;
        }
        if (ROLE_INSPECTOR.equals(toRemove.getRole()) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Solo el administrador puede remover al inspector asignado");
        }
        String removedEmail = toRemove.getUserEmail();
        String removedRole = toRemove.getRole();
        assignmentRepository.delete(toRemove);
        String actor = callerEmail != null ? callerEmail.trim().toLowerCase() : "";
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("assigneeEmail", removedEmail);
        meta.put("role", removedRole);
        try {
            auditService.log(actor, "Inspection", inspectionId, "ASSIGNMENT_REMOVE",
                    objectMapper.writeValueAsString(meta));
        } catch (JsonProcessingException e) {
            auditService.log(actor, "Inspection", inspectionId, "ASSIGNMENT_REMOVE", null);
        }
    }
}
