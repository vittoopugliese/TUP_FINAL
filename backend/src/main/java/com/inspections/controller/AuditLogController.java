package com.inspections.controller;

import com.inspections.entity.AuditLog;
import com.inspections.repository.AuditLogRepository;
import com.inspections.service.AuditLogReportPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Listado de registros de auditoría (solo ADMIN).
 */
@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit logs", description = "Registro de auditoría del sistema")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogReportPdfService pdfService;

    public AuditLogController(AuditLogRepository auditLogRepository,
                               AuditLogReportPdfService pdfService) {
        this.auditLogRepository = auditLogRepository;
        this.pdfService = pdfService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar auditoría", description = "Filtros opcionales por acción, usuario (email), entidad (ID) y rango de fechas (ISO-8601 instant)")
    public ResponseEntity<List<AuditLog>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String actionFilter = (action == null || action.isBlank()) ? null : action.trim();
        String userIdFilter = (userId == null || userId.isBlank()) ? null : userId.trim().toLowerCase();
        String entityIdFilter = (entityId == null || entityId.isBlank()) ? null : entityId.trim();
        List<AuditLog> rows = auditLogRepository.findByFilters(actionFilter, userIdFilter, entityIdFilter, from, to);
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/report/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar auditoría a PDF", description = "Genera un PDF con los registros filtrados")
    public ResponseEntity<?> exportPdf(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        String actionFilter = (action == null || action.isBlank()) ? null : action.trim();
        String userIdFilter = (userId == null || userId.isBlank()) ? null : userId.trim().toLowerCase();
        String entityIdFilter = (entityId == null || entityId.isBlank()) ? null : entityId.trim();

        List<AuditLog> rows = auditLogRepository.findByFilters(actionFilter, userIdFilter, entityIdFilter, from, to);

        List<String> parts = new ArrayList<>();
        if (actionFilter != null) parts.add("Acción: " + actionFilter);
        if (userIdFilter != null) parts.add("Usuario: " + userIdFilter);
        if (entityIdFilter != null) parts.add("Entidad: " + entityIdFilter);
        if (from != null) parts.add("Desde: " + from);
        if (to != null) parts.add("Hasta: " + to);
        String filtersSummary = parts.isEmpty() ? "Ninguno" : String.join(" | ", parts);

        byte[] pdfBytes = pdfService.generatePdf(rows, filtersSummary);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-report.pdf\"")
                .body(resource);
    }
}
