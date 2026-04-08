package com.inspections.service;

import com.inspections.dto.report.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera el PDF del reporte de inspección a partir de {@link InspectionReportData}.
 * <p>
 * Diseño compacto en formato tabular (A4 apaisado), paleta rojo-negro-blanco,
 * logo opcional desde {@code classpath:/report/logo.png}. Sin saltos de página
 * artificiales: solo el desbordamiento natural del contenido.
 */
@Service
public class InspectionReportPdfService {

    private static final String LOGO_RESOURCE = "/report/logo.png";

    /** #B71C1C — alineado con obs_deficiency_color en la app */
    private static final Color RED_BRAND = new Color(183, 28, 28);
    /** #B00020 — design_default_color_error */
    private static final Color RED_ACCENT = new Color(176, 0, 32);
    private static final Color BLACK = new Color(20, 20, 20);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER_GRAY = new Color(200, 200, 200);

    private static final float MARGIN = 20f;
    private static final float CELL_PADDING = 3f;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"));

    private final Font fontTitle;
    private final Font fontSection;
    private final Font fontHeaderWhite;
    private final Font fontNormal;
    private final Font fontSmall;
    private final Font fontTiny;
    private final Font fontMetaKey;
    private final Font fontMetaVal;
    private final Font fontObsRed;
    private final Font fontFooterNote;

    public InspectionReportPdfService() {
        fontTitle = new Font(Font.HELVETICA, 16, Font.BOLD, RED_BRAND);
        fontSection = new Font(Font.HELVETICA, 10, Font.BOLD, BLACK);
        fontHeaderWhite = new Font(Font.HELVETICA, 8, Font.BOLD, WHITE);
        fontNormal = new Font(Font.HELVETICA, 7, Font.NORMAL, BLACK);
        fontSmall = new Font(Font.HELVETICA, 6, Font.NORMAL, BLACK);
        fontTiny = new Font(Font.HELVETICA, 5, Font.NORMAL, new Color(80, 80, 80));
        fontMetaKey = new Font(Font.HELVETICA, 8, Font.BOLD, RED_BRAND);
        fontMetaVal = new Font(Font.HELVETICA, 8, Font.NORMAL, BLACK);
        fontObsRed = new Font(Font.HELVETICA, 6, Font.BOLD, RED_ACCENT);
        fontFooterNote = new Font(Font.HELVETICA, 6, Font.ITALIC, new Color(100, 100, 100));
    }

    public byte[] generatePdf(InspectionReportData data) {
        Rectangle pageSize = PageSize.A4.rotate();
        Document document = new Document(pageSize, MARGIN, MARGIN, MARGIN, MARGIN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addBrandedHeader(document, data);
            addMetaGrid(document, data);
            addAssignmentsBlock(document, data);
            addSummaryBlock(document, data);
            addDetailTable(document, data);
            addFooterNote(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    private void addBrandedHeader(Document document, InspectionReportData data) throws DocumentException {
        PdfPTable outer = new PdfPTable(2);
        outer.setWidthPercentage(100);
        outer.setWidths(new float[]{22f, 78f});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingBottom(4f);
        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(56f, 56f);
            logoCell.addElement(logo);
        } else {
            logoCell.addElement(new Phrase(" ", fontSmall));
        }
        outer.addCell(logoCell);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph title = new Paragraph("Reporte de inspección", fontTitle);
        title.setSpacingAfter(2f);
        titleCell.addElement(title);

        boolean approved = "DONE_COMPLETED".equals(data.getStatus());
        String resultLabel = approved ? "APROBADA" : "REPROBADA";
        Font resultFont = new Font(Font.HELVETICA, 11, Font.BOLD, approved ? BLACK : RED_BRAND);
        titleCell.addElement(new Paragraph("Resultado: " + resultLabel, resultFont));

        PdfPCell bar = new PdfPCell();
        bar.setBorder(Rectangle.NO_BORDER);
        bar.setFixedHeight(3f);
        bar.setBackgroundColor(RED_BRAND);
        PdfPTable barWrap = new PdfPTable(1);
        barWrap.setWidthPercentage(100);
        barWrap.addCell(bar);

        PdfPCell wrapCell = new PdfPCell();
        wrapCell.setBorder(Rectangle.NO_BORDER);
        wrapCell.addElement(outer);
        wrapCell.addElement(barWrap);
        wrapCell.setPaddingBottom(6f);

        PdfPTable shell = new PdfPTable(1);
        shell.setWidthPercentage(100);
        shell.addCell(wrapCell);
        document.add(shell);
    }

    private Image loadLogo() {
        try (InputStream in = InspectionReportPdfService.class.getResourceAsStream(LOGO_RESOURCE)) {
            if (in == null) {
                return null;
            }
            byte[] bytes = in.readAllBytes();
            if (bytes.length == 0) {
                return null;
            }
            return Image.getInstance(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private void addMetaGrid(Document document, InspectionReportData data) throws DocumentException {
        PdfPTable grid = new PdfPTable(4);
        grid.setWidthPercentage(100);
        grid.setWidths(new float[]{18f, 32f, 18f, 32f});
        grid.setSpacingAfter(6f);

        addMetaRow(grid, "Tipo", nullToEmpty(data.getType()), "Edificio", nullToEmpty(data.getBuildingName()));
        addMetaRow(grid, "ID inspección", nullToEmpty(data.getInspectionId()), "Resultado técnico", nullToEmpty(data.getResult()));
        addMetaRow(grid, "Programada", formatInstant(data.getScheduledDate()), "Inicio", formatInstant(data.getStartedAt()));
        addMetaRow(grid, "Firma", formatInstant(data.getSignDate()), "Firmante", nullToEmpty(data.getSigner()));

        if (data.getNotes() != null && !data.getNotes().isBlank()) {
            PdfPCell k = metaKeyCell("Notas");
            PdfPCell v = metaValCell(data.getNotes());
            v.setColspan(3);
            grid.addCell(k);
            grid.addCell(v);
        }

        document.add(grid);
    }

    private void addMetaRow(PdfPTable grid, String k1, String v1, String k2, String v2) {
        grid.addCell(metaKeyCell(k1));
        grid.addCell(metaValCell(v1));
        grid.addCell(metaKeyCell(k2));
        grid.addCell(metaValCell(v2));
    }

    private PdfPCell metaKeyCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, fontMetaKey));
        styleMetaCell(c);
        c.setBackgroundColor(new Color(255, 245, 245));
        return c;
    }

    private PdfPCell metaValCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "—", fontMetaVal));
        styleMetaCell(c);
        return c;
    }

    private void styleMetaCell(PdfPCell c) {
        c.setPadding(CELL_PADDING);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(BORDER_GRAY);
    }

    private void addAssignmentsBlock(Document document, InspectionReportData data) throws DocumentException {
        Paragraph p = new Paragraph("Asignaciones", fontSection);
        p.setSpacingAfter(3f);
        document.add(p);

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{18f, 82f});
        t.setSpacingAfter(6f);

        t.addCell(headerCell("Rol", 1));
        t.addCell(headerCell("Correo(s)", 1));
        addAssignmentRow(t, "Inspector(es)", joinEmails(data.getInspectorEmails()));
        addAssignmentRow(t, "Operador(es)", joinEmails(data.getOperatorEmails()));

        document.add(t);
    }

    private static String joinEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return "—";
        }
        return String.join(", ", emails);
    }

    private void addAssignmentRow(PdfPTable t, String role, String emails) {
        t.addCell(bodyCell(role, true));
        t.addCell(bodyCell(emails, false));
    }

    private void addSummaryBlock(Document document, InspectionReportData data) throws DocumentException {
        Paragraph p = new Paragraph("Resumen ejecutivo", fontSection);
        p.setSpacingAfter(3f);
        document.add(p);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(55);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidths(new float[]{62f, 38f});
        table.setSpacingAfter(8f);

        table.addCell(headerCell("Concepto", 1));
        table.addCell(headerCell("Cant.", 1));

        addSummaryRow(table, "Ubicaciones", data.getLocationCount());
        addSummaryRow(table, "Zonas", data.getZoneCount());
        addSummaryRow(table, "Dispositivos", data.getDeviceCount());
        addSummaryRow(table, "Tests", data.getTestCount());
        addSummaryRow(table, "Steps", data.getStepCount());
        addSummaryRow(table, "Observaciones", data.getObservationCount());
        addSummaryRow(table, "Deficiencias", data.getDeficiencyCount());
        addSummaryRow(table, "Fotos adjuntas", data.getPhotoCount());

        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, int value) {
        table.addCell(bodyCell(label, false));
        table.addCell(bodyCell(String.valueOf(value), false));
    }

    private void addDetailTable(Document document, InspectionReportData data) throws DocumentException {
        Paragraph p = new Paragraph("Detalle técnico (por ubicación, zona, dispositivo, test y step)", fontSection);
        p.setSpacingAfter(4f);
        document.add(p);

        if (data.getLocations() == null || data.getLocations().isEmpty()) {
            document.add(new Paragraph("Sin ubicaciones registradas para este edificio.", fontNormal));
            return;
        }

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10f, 10f, 12f, 14f, 14f, 9f, 31f});
        table.setHeaderRows(1);
        table.setSpacingBefore(2f);
        table.setSplitLate(false);
        table.setSplitRows(true);

        String[] headers = {"Ubicación", "Zona", "Dispositivo", "Test", "Step", "Valor", "Observación"};
        for (String h : headers) {
            table.addCell(headerCell(h, 1));
        }

        int rows = 0;
        for (ReportLocationData loc : data.getLocations()) {
            for (ReportZoneData zone : loc.getZones()) {
                for (ReportDeviceData dev : zone.getDevices()) {
                    for (ReportTestData test : dev.getTests()) {
                        for (ReportStepData step : test.getSteps()) {
                            String locStr = formatLocation(loc);
                            String zoneStr = formatZone(zone);
                            String devStr = formatDevice(dev);
                            String testStr = formatTest(test);
                            String stepStr = formatStep(step);
                            String valStr = formatValue(step);

                            if (step.getObservations() == null || step.getObservations().isEmpty()) {
                                table.addCell(bodyCell(locStr, false));
                                table.addCell(bodyCell(zoneStr, false));
                                table.addCell(bodyCell(devStr, false));
                                table.addCell(bodyCell(testStr, false));
                                table.addCell(bodyCell(stepStr, false));
                                table.addCell(bodyCell(valStr, false));
                                table.addCell(bodyCell("—", false));
                                rows++;
                            } else {
                                for (ReportObservationData obs : step.getObservations()) {
                                    table.addCell(bodyCell(locStr, false));
                                    table.addCell(bodyCell(zoneStr, false));
                                    table.addCell(bodyCell(devStr, false));
                                    table.addCell(bodyCell(testStr, false));
                                    table.addCell(bodyCell(stepStr, false));
                                    table.addCell(bodyCell(valStr, false));
                                    table.addCell(observationCell(obs));
                                    rows++;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (rows == 0) {
            document.add(new Paragraph("No hay steps registrados en la jerarquía de esta inspección.", fontNormal));
            return;
        }

        document.add(table);
    }

    private PdfPCell observationCell(ReportObservationData obs) {
        Phrase phrase = new Phrase();
        boolean def = "DEFICIENCIES".equals(obs.getType());
        Font typeFont = def ? fontObsRed : fontSmall;
        phrase.add(new Chunk(nullToEmpty(obs.getType()) + ": ", typeFont));
        phrase.add(new Chunk(nullToEmpty(obs.getDescription()), fontSmall));
        if (obs.getName() != null && !obs.getName().isBlank()) {
            phrase.add(new Chunk("\nNombre: " + obs.getName(), fontTiny));
        }
        if (obs.getDeficiencyTypeName() != null && !obs.getDeficiencyTypeName().isBlank()) {
            phrase.add(new Chunk("\nTipo def.: " + obs.getDeficiencyTypeName(), fontTiny));
        }
        if (obs.getPhotoMetadata() != null && !obs.getPhotoMetadata().isBlank()) {
            phrase.add(new Chunk("\nFoto: " + obs.getPhotoMetadata(), fontTiny));
        }
        if (obs.getMediaUrl() != null && !obs.getMediaUrl().isBlank()) {
            phrase.add(new Chunk("\nURL: " + obs.getMediaUrl(), fontTiny));
        }
        PdfPCell c = new PdfPCell(phrase);
        styleBodyCell(c);
        return c;
    }

    private String formatLocation(ReportLocationData loc) {
        StringBuilder sb = new StringBuilder(nullToEmpty(loc.getName()));
        if (loc.getDetails() != null && !loc.getDetails().isBlank()) {
            sb.append("\n").append(loc.getDetails());
        }
        return sb.toString();
    }

    private String formatZone(ReportZoneData zone) {
        StringBuilder sb = new StringBuilder(nullToEmpty(zone.getName()));
        if (zone.getDetails() != null && !zone.getDetails().isBlank()) {
            sb.append("\n").append(zone.getDetails());
        }
        return sb.toString();
    }

    private String formatDevice(ReportDeviceData dev) {
        StringBuilder sb = new StringBuilder(nullToEmpty(dev.getName()));
        if (dev.getDeviceCategory() != null && !dev.getDeviceCategory().isBlank()) {
            sb.append(" (").append(dev.getDeviceCategory()).append(")");
        }
        if (dev.getDeviceSerialNumber() != null) {
            sb.append("\nSerie: ").append(dev.getDeviceSerialNumber());
        }
        if (dev.getDescription() != null && !dev.getDescription().isBlank()) {
            sb.append("\n").append(dev.getDescription());
        }
        return sb.toString();
    }

    private String formatTest(ReportTestData test) {
        StringBuilder sb = new StringBuilder(nullToEmpty(test.getName()));
        sb.append("\nEstado test: ").append(nullToEmpty(test.getStatus()));
        if (test.getDescription() != null && !test.getDescription().isBlank()) {
            sb.append("\n").append(test.getDescription());
        }
        if (test.getCreatedAt() != null) {
            sb.append("\nCreado: ").append(test.getCreatedAt().atZone(ZoneId.systemDefault()).format(DATE_FORMAT));
        }
        return sb.toString();
    }

    private String formatStep(ReportStepData step) {
        StringBuilder sb = new StringBuilder(nullToEmpty(step.getName()));
        sb.append("\nTipo: ").append(nullToEmpty(step.getTestStepType()));
        sb.append(" | ").append(step.isApplicable() ? "Aplica" : "N/A");
        sb.append(" | Estado: ").append(nullToEmpty(step.getStatus()));
        if (step.getDescription() != null && !step.getDescription().isBlank()) {
            sb.append("\n").append(step.getDescription());
        }
        if (step.getMinValue() != null || step.getMaxValue() != null) {
            sb.append("\nRango: ");
            if (step.getMinValue() != null) {
                sb.append(step.getMinValue());
            }
            sb.append(" – ");
            if (step.getMaxValue() != null) {
                sb.append(step.getMaxValue());
            }
        }
        if (step.getCreatedAt() != null) {
            sb.append("\nCreado: ").append(step.getCreatedAt().atZone(ZoneId.systemDefault()).format(DATE_FORMAT));
        }
        return sb.toString();
    }

    private String formatValue(ReportStepData step) {
        if (step.getValueJson() != null && !step.getValueJson().isBlank()) {
            return step.getValueJson();
        }
        return "—";
    }

    private PdfPCell headerCell(String text, int colspan) {
        PdfPCell c = new PdfPCell(new Phrase(text, fontHeaderWhite));
        c.setBackgroundColor(RED_BRAND);
        c.setPadding(CELL_PADDING);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(RED_BRAND);
        if (colspan > 1) {
            c.setColspan(colspan);
        }
        return c;
    }

    private PdfPCell bodyCell(String text, boolean isLabel) {
        Font f = isLabel ? fontMetaKey : fontNormal;
        PdfPCell c = new PdfPCell(new Phrase(text != null ? text : "—", f));
        styleBodyCell(c);
        return c;
    }

    private void styleBodyCell(PdfPCell c) {
        c.setPadding(CELL_PADDING);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(BORDER_GRAY);
        c.setBackgroundColor(WHITE);
    }

    private void addFooterNote(Document document) throws DocumentException {
        Paragraph note = new Paragraph(
                "Nota: las imágenes adjuntas podrían incluirse como anexo en una futura versión.",
                fontFooterNote);
        note.setSpacingBefore(10f);
        document.add(note);
    }

    private String formatInstant(java.time.Instant instant) {
        if (instant == null) {
            return "—";
        }
        return instant.atZone(ZoneId.systemDefault()).format(DATE_FORMAT);
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "—";
    }
}
