package com.inspections.controller;

import com.inspections.service.InspectionReportPdfService;
import com.inspections.service.InspectionReportService;
import com.inspections.dto.report.InspectionReportData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint para descargar el reporte PDF de una inspección firmada y cerrada.
 * Solo visible para inspectores y operadores asignados.
 */
@RestController
@RequestMapping("/api/inspections")
@Tag(name = "Inspection Report", description = "Descarga de reportes PDF de inspecciones")
public class InspectionReportController {

    private final InspectionReportService reportService;
    private final InspectionReportPdfService pdfService;

    public InspectionReportController(InspectionReportService reportService,
                                      InspectionReportPdfService pdfService) {
        this.reportService = reportService;
        this.pdfService = pdfService;
    }

    @GetMapping("/{id}/report/pdf")
    @Operation(summary = "Descargar reporte PDF",
               description = "Genera y descarga el reporte PDF de la inspección. Solo para inspecciones firmadas (DONE_*) y usuarios asignados como inspector u operador.")
    public ResponseEntity<?> getReportPdf(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getPrincipal().toString();

        InspectionReportData data = reportService.buildReportData(id, email);
        byte[] pdfBytes = pdfService.generatePdf(data);

        String filename = "inspection-" + id + ".pdf";
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
