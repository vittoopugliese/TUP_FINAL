package com.inspections.service;

import com.inspections.dto.report.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera el PDF del reporte de inspección a partir de InspectionReportData.
 * <p>
 * Fase 1: reporte textual completo sin imágenes embebidas.
 * Estrategia futura para fotos embebidas (fase 2): resolver mediaId desde
 * observaciones vía PhotoRepository, cargar bytes de imagen y usar
 * {@code Image.getInstance(bytes)} + {@code document.add(Image)} en la sección
 * de observaciones o en un anexo fotográfico.
 */
@Service
public class InspectionReportPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font FONT_HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_SUBHEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8);

    public byte[] generatePdf(InspectionReportData data) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addCoverSection(document, data);
            document.newPage();
            addAssignmentsSection(document, data);
            addSummarySection(document, data);
            document.newPage();
            addHierarchySection(document, data);
            addDeficienciesSection(document, data);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    private void addCoverSection(Document document, InspectionReportData data) throws DocumentException {
        document.add(new Paragraph("Reporte de inspección", FONT_TITLE));
        document.add(new Paragraph(" "));

        String resultLabel = "DONE_COMPLETED".equals(data.getStatus()) ? "APROBADA" : "REPROBADA";
        document.add(new Paragraph("Resultado: " + resultLabel, FONT_HEADING));
        document.add(new Paragraph("Tipo: " + nullToEmpty(data.getType())));
        document.add(new Paragraph("Edificio: " + nullToEmpty(data.getBuildingName())));
        document.add(new Paragraph("Fecha programada: " + formatInstant(data.getScheduledDate())));
        document.add(new Paragraph("Fecha de inicio: " + formatInstant(data.getStartedAt())));
        document.add(new Paragraph("Fecha de firma: " + formatInstant(data.getSignDate())));
        document.add(new Paragraph("Firmante: " + nullToEmpty(data.getSigner())));
        document.add(new Paragraph("Resultado técnico: " + nullToEmpty(data.getResult())));
        document.add(new Paragraph("ID inspección: " + nullToEmpty(data.getInspectionId())));
        if (data.getNotes() != null && !data.getNotes().isBlank()) {
            document.add(new Paragraph("Notas: " + data.getNotes()));
        }
    }

    private void addAssignmentsSection(Document document, InspectionReportData data) throws DocumentException {
        document.add(new Paragraph("Asignaciones", FONT_HEADING));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Inspector(es): " + String.join(", ", data.getInspectorEmails()), FONT_NORMAL));
        document.add(new Paragraph("Operador(es): " + String.join(", ", data.getOperatorEmails()), FONT_NORMAL));
        document.add(new Paragraph(" "));
    }

    private void addSummarySection(Document document, InspectionReportData data) throws DocumentException {
        document.add(new Paragraph("Resumen ejecutivo", FONT_HEADING));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setWidths(new int[]{3, 1});
        table.setSpacingBefore(4f);
        table.setHeaderRows(1);
        PdfPCell headerLabel = new PdfPCell(new Phrase("Concepto", FONT_SUBHEADING));
        PdfPCell headerValue = new PdfPCell(new Phrase("Cantidad", FONT_SUBHEADING));
        table.addCell(headerLabel);
        table.addCell(headerValue);
        addSummaryRow(table, "Ubicaciones", data.getLocationCount());
        addSummaryRow(table, "Zonas", data.getZoneCount());
        addSummaryRow(table, "Dispositivos", data.getDeviceCount());
        addSummaryRow(table, "Tests", data.getTestCount());
        addSummaryRow(table, "Steps", data.getStepCount());
        addSummaryRow(table, "Observaciones", data.getObservationCount());
        addSummaryRow(table, "Deficiencias", data.getDeficiencyCount());
        addSummaryRow(table, "Fotos adjuntas", data.getPhotoCount());
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addSummaryRow(PdfPTable table, String label, int value) {
        table.addCell(new PdfPCell(new Phrase(label, FONT_NORMAL)));
        table.addCell(new PdfPCell(new Phrase(String.valueOf(value), FONT_NORMAL)));
    }

    private void addHierarchySection(Document document, InspectionReportData data) throws DocumentException {
        document.add(new Paragraph("Detalle jerárquico", FONT_HEADING));
        document.add(new Paragraph(" "));

        int idx = 0;
        for (ReportLocationData loc : data.getLocations()) {
            if (idx++ > 0) document.newPage();
            document.add(new Paragraph("Ubicación: " + nullToEmpty(loc.getName()), FONT_SUBHEADING));
            if (loc.getDetails() != null && !loc.getDetails().isBlank()) {
                document.add(new Paragraph("  Detalles: " + loc.getDetails(), FONT_SMALL));
            }

            for (ReportZoneData zone : loc.getZones()) {
                document.add(new Paragraph("  Zona: " + nullToEmpty(zone.getName()), FONT_NORMAL));
                if (zone.getDetails() != null && !zone.getDetails().isBlank()) {
                    document.add(new Paragraph("    Detalles: " + zone.getDetails(), FONT_SMALL));
                }

                for (ReportDeviceData dev : zone.getDevices()) {
                    document.add(new Paragraph("    Dispositivo: " + nullToEmpty(dev.getName()) +
                            (dev.getDeviceCategory() != null ? " (" + dev.getDeviceCategory() + ")" : ""), FONT_NORMAL));

                    for (ReportTestData test : dev.getTests()) {
                        document.add(new Paragraph("      Test: " + nullToEmpty(test.getName()) +
                                " | Estado: " + nullToEmpty(test.getStatus()), FONT_NORMAL));
                        if (test.getDescription() != null && !test.getDescription().isBlank()) {
                            document.add(new Paragraph("        " + test.getDescription(), FONT_SMALL));
                        }

                        for (ReportStepData step : test.getSteps()) {
                            String appLabel = step.isApplicable() ? "" : " [N/A]";
                            document.add(new Paragraph("        Step: " + nullToEmpty(step.getName()) +
                                    appLabel + " | " + nullToEmpty(step.getTestStepType()) +
                                    " | Estado: " + nullToEmpty(step.getStatus()), FONT_SMALL));
                            if (step.getValueJson() != null && !step.getValueJson().isBlank()) {
                                document.add(new Paragraph("          Valor: " + step.getValueJson(), FONT_SMALL));
                            }

                            for (ReportObservationData obs : step.getObservations()) {
                                document.add(new Paragraph("          - " + nullToEmpty(obs.getType()) + ": " +
                                        nullToEmpty(obs.getDescription()), FONT_SMALL));
                                if (obs.getDeficiencyTypeName() != null) {
                                    document.add(new Paragraph("            Tipo deficiencia: " + obs.getDeficiencyTypeName(), FONT_SMALL));
                                }
                                if (obs.getPhotoMetadata() != null && !obs.getPhotoMetadata().isBlank()) {
                                    document.add(new Paragraph("            Foto: " + obs.getPhotoMetadata(), FONT_SMALL));
                                }
                            }
                        }
                    }
                }
            }
            document.add(new Paragraph(" "));
        }
    }

    private void addDeficienciesSection(Document document, InspectionReportData data) throws DocumentException {
        if (data.getDeficiencyCount() == 0) return;

        document.newPage();
        document.add(new Paragraph("Deficiencias detectadas", FONT_HEADING));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{3, 2, 3, 2});
        table.setHeaderRows(1);
        table.addCell(headerCell("Ubicación / Zona / Dispositivo"));
        table.addCell(headerCell("Test / Step"));
        table.addCell(headerCell("Descripción"));
        table.addCell(headerCell("Tipo"));

        for (ReportLocationData loc : data.getLocations()) {
            for (ReportZoneData zone : loc.getZones()) {
                for (ReportDeviceData dev : zone.getDevices()) {
                    for (ReportTestData test : dev.getTests()) {
                        for (ReportStepData step : test.getSteps()) {
                            for (ReportObservationData obs : step.getObservations()) {
                                if ("DEFICIENCIES".equals(obs.getType())) {
                                    String path = nullToEmpty(loc.getName()) + " / " + nullToEmpty(zone.getName()) +
                                            " / " + nullToEmpty(dev.getName());
                                    String testStep = nullToEmpty(test.getName()) + " / " + nullToEmpty(step.getName());
                                    table.addCell(cell(path));
                                    table.addCell(cell(testStep));
                                    table.addCell(cell(nullToEmpty(obs.getDescription())));
                                    table.addCell(cell(obs.getDeficiencyTypeName() != null ? obs.getDeficiencyTypeName() : "—"));
                                }
                            }
                        }
                    }
                }
            }
        }
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Nota: En una futura versión se incluirán las imágenes adjuntas en el reporte.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7)));
    }

    private PdfPCell headerCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_SUBHEADING));
        c.setBackgroundColor(new java.awt.Color(230, 230, 230));
        return c;
    }

    private PdfPCell cell(String text) {
        return new PdfPCell(new Phrase(text != null ? text : "—", FONT_SMALL));
    }

    private String formatInstant(java.time.Instant instant) {
        if (instant == null) return "—";
        return instant.atZone(ZoneId.systemDefault()).format(DATE_FORMAT);
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "—";
    }
}
