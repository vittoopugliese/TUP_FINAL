package com.inspections.controller;

import com.inspections.entity.AuditLog;
import com.inspections.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Listado de registros de auditoría (solo ADMIN).
 */
@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit logs", description = "Registro de auditoría del sistema")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar auditoría", description = "Filtros opcionales por acción y rango de fechas (ISO-8601 instant)")
    public ResponseEntity<List<AuditLog>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String actionFilter = (action == null || action.isBlank()) ? null : action.trim();
        List<AuditLog> rows = auditLogRepository.findByFilters(actionFilter, from, to);
        return ResponseEntity.ok(rows);
    }
}
