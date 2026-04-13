package com.inspections.service;

import com.inspections.entity.AuditLog;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Genera un PDF tabular con registros de auditoría filtrados.
 * Paleta y estilo alineados con {@link InspectionReportPdfService}.
 */
@Service
public class AuditLogReportPdfService {

    private static final Color RED_BRAND = new Color(183, 28, 28);
    private static final Color BLACK = new Color(20, 20, 20);
    private static final Color WHITE = Color.WHITE;
    private static final Color BORDER_GRAY = new Color(200, 200, 200);
    private static final Color ROW_ALT = new Color(245, 245, 245);

    private static final float MARGIN = 20f;
    private static final float CELL_PADDING = 4f;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"))
                    .withZone(ZoneId.of("America/Argentina/Buenos_Aires"));

    private final Font fontTitle;
    private final Font fontFilters;
    private final Font fontHeaderWhite;
    private final Font fontNormal;
    private final Font fontSmall;
    private final Font fontFooter;

    public AuditLogReportPdfService() {
        fontTitle = new Font(Font.HELVETICA, 14, Font.BOLD, RED_BRAND);
        fontFilters = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(80, 80, 80));
        fontHeaderWhite = new Font(Font.HELVETICA, 8, Font.BOLD, WHITE);
        fontNormal = new Font(Font.HELVETICA, 7, Font.NORMAL, BLACK);
        fontSmall = new Font(Font.HELVETICA, 6, Font.NORMAL, new Color(80, 80, 80));
        fontFooter = new Font(Font.HELVETICA, 6, Font.ITALIC, new Color(100, 100, 100));
    }

    public byte[] generatePdf(List<AuditLog> logs, String filtersSummary) {
        Rectangle pageSize = PageSize.A4.rotate();
        Document document = new Document(pageSize, MARGIN, MARGIN, MARGIN, MARGIN);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Paragraph title = new Paragraph("Informe de Auditoría", fontTitle);
            title.setSpacingAfter(4f);
            document.add(title);

            if (filtersSummary != null && !filtersSummary.isBlank()) {
                Paragraph filters = new Paragraph("Filtros: " + filtersSummary, fontFilters);
                filters.setSpacingAfter(6f);
                document.add(filters);
            }

            Paragraph countPara = new Paragraph("Registros: " + logs.size(), fontSmall);
            countPara.setSpacingAfter(8f);
            document.add(countPara);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{14f, 20f, 14f, 10f, 14f, 28f});

            addHeaderCell(table, "Fecha");
            addHeaderCell(table, "Usuario");
            addHeaderCell(table, "Acción");
            addHeaderCell(table, "Tipo Entidad");
            addHeaderCell(table, "ID Entidad");
            addHeaderCell(table, "Metadata");

            for (int i = 0; i < logs.size(); i++) {
                AuditLog row = logs.get(i);
                Color bg = (i % 2 == 0) ? WHITE : ROW_ALT;

                addDataCell(table, formatDate(row), bg);
                addDataCell(table, safe(row.getUserId()), bg);
                addDataCell(table, safe(row.getAction()), bg);
                addDataCell(table, safe(row.getEntityType()), bg);
                addDataCell(table, truncate(safe(row.getEntityId()), 20), bg);
                addDataCell(table, truncate(safe(row.getMetadataJson()), 80), bg);
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Generado automáticamente. Este informe refleja los registros al momento de la consulta.",
                    fontFooter);
            footer.setSpacingBefore(8f);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de auditoría", e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, fontHeaderWhite));
        cell.setBackgroundColor(RED_BRAND);
        cell.setPadding(CELL_PADDING);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BORDER_GRAY);
        table.addCell(cell);
    }

    private void addDataCell(PdfPTable table, String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, fontNormal));
        cell.setBackgroundColor(bg);
        cell.setPadding(CELL_PADDING);
        cell.setBorderColor(BORDER_GRAY);
        table.addCell(cell);
    }

    private String formatDate(AuditLog row) {
        if (row.getCreatedAt() == null) return "";
        return DATE_FMT.format(row.getCreatedAt());
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max) + "…";
    }
}
