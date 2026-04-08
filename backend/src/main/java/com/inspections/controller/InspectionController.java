package com.inspections.controller;

import com.inspections.dto.CreateInspectionRequest;
import com.inspections.dto.CreateInspectionResponse;
import com.inspections.dto.InspectionListResponse;
import com.inspections.dto.SignInspectionRequest;
import com.inspections.service.InspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de inspecciones.
 *
 * GET /api/inspections – Lista inspecciones (filtradas por rol: ADMIN=todas, INSPECTOR/OPERATOR=solo asignadas)
 * POST /api/inspections – Crea una inspección building-wide
 */
@RestController
@RequestMapping("/api/inspections")
@Tag(name = "Inspections", description = "Operaciones de inspecciones")
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping
    @Operation(summary = "Listar inspecciones",
               description = "ADMIN: todas. INSPECTOR/OPERATOR: solo asignadas. Requiere autenticación.")
    public ResponseEntity<?> getInspections() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getPrincipal().toString();
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("INSPECTOR");
        List<InspectionListResponse> inspections = inspectionService.getInspectionsForCurrentUser(email, role);
        return ResponseEntity.ok(inspections);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
    @Operation(summary = "Crear inspección",
               description = "Crea una inspección building-wide con snapshot de locations, zones, devices y tests. Solo ADMIN e INSPECTOR.")
    public ResponseEntity<CreateInspectionResponse> createInspection(
            @Valid @RequestBody CreateInspectionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getPrincipal().toString();
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("INSPECTOR");
        CreateInspectionResponse created = inspectionService.createInspection(request, email, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Iniciar inspección",
               description = "Transiciona la inspección de PENDING/SCHEDULED a IN_PROGRESS. "
                       + "Requiere al menos 1 Inspector asignado.")
    public ResponseEntity<InspectionListResponse> startInspection(@PathVariable String id) {
        InspectionListResponse started = inspectionService.startInspection(id);
        return ResponseEntity.ok(started);
    }

    @PostMapping("/{id}/sign")
    @Operation(summary = "Firmar inspección",
               description = "Firma digital: valida que todos los tests estén COMPLETED o FAILED "
                       + "y que el firmante sea el Inspector asignado. Transiciona la inspección a DONE_*.")
    public ResponseEntity<?> signInspection(
            @PathVariable String id,
            @Valid @RequestBody SignInspectionRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getPrincipal().toString();

        InspectionListResponse signed = inspectionService.signInspection(
                id, request.getSignerName(), email);
        return ResponseEntity.ok(signed);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Obtener estado de inspección",
               description = "Recalcula el estado de la inspección a partir de los tests y retorna el resultado. "
                       + "Si todos los tests son COMPLETED → DONE_COMPLETED. "
                       + "Si algún test es FAILED → DONE_FAILED. "
                       + "Si quedan tests PENDING → permanece IN_PROGRESS.")
    public ResponseEntity<InspectionListResponse> getInspectionStatus(@PathVariable String id) {
        InspectionListResponse status = inspectionService.recalculateAndGetStatus(id);
        return ResponseEntity.ok(status);
    }
}
