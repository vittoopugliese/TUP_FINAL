package com.inspections.service;

import com.inspections.dto.report.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica que el PDF se genere sin errores y con cabecera PDF válida.
 */
class InspectionReportPdfServiceTest {

    private final InspectionReportPdfService pdfService = new InspectionReportPdfService();

    @Test
    void generatePdf_minimalData_producesValidPdfHeader() {
        InspectionReportData data = baseData();
        data.setLocations(new ArrayList<>());

        byte[] pdf = pdfService.generatePdf(data);
        assertPdfMagic(pdf);
    }

    @Test
    void generatePdf_withHierarchy_producesValidPdf() {
        InspectionReportData data = baseData();
        data.setLocations(List.of(buildLocationTree()));
        data.setLocationCount(1);
        data.setZoneCount(1);
        data.setDeviceCount(1);
        data.setTestCount(1);
        data.setStepCount(1);
        data.setObservationCount(2);
        data.setDeficiencyCount(1);
        data.setPhotoCount(0);

        byte[] pdf = pdfService.generatePdf(data);
        assertPdfMagic(pdf);
        assertTrue(pdf.length > 500, "PDF debería tener tamaño razonable con detalle");
    }

    private static void assertPdfMagic(byte[] pdf) {
        assertNotNull(pdf);
        assertTrue(pdf.length > 50);
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    private static InspectionReportData baseData() {
        InspectionReportData data = new InspectionReportData();
        data.setInspectionId("insp-test");
        data.setBuildingId("b1");
        data.setBuildingName("Edificio prueba");
        data.setType("ANUAL");
        data.setStatus("DONE_COMPLETED");
        data.setResult("OK");
        data.setSigned(true);
        data.setInspectorEmails(List.of("inspector@example.com"));
        data.setOperatorEmails(List.of("operador@inspections.com"));
        data.setScheduledDate(Instant.parse("2025-01-15T10:00:00Z"));
        data.setStartedAt(Instant.parse("2025-01-15T10:30:00Z"));
        data.setSignDate(Instant.parse("2025-01-15T12:00:00Z"));
        data.setSigner("Inspector Test");
        return data;
    }

    private static ReportLocationData buildLocationTree() {
        ReportObservationData obs1 = new ReportObservationData();
        obs1.setType("REMARK");
        obs1.setDescription("Observación general");
        obs1.setName("Obs1");

        ReportObservationData obs2 = new ReportObservationData();
        obs2.setType("DEFICIENCIES");
        obs2.setDescription("Falta extintor");
        obs2.setDeficiencyTypeName("Tipo A");

        ReportStepData step = new ReportStepData();
        step.setName("Paso 1");
        step.setTestStepType("BINARY");
        step.setApplicable(true);
        step.setStatus("COMPLETED");
        step.setValueJson("{\"ok\":true}");
        step.setCreatedAt(Instant.parse("2025-01-15T11:00:00Z"));
        step.setObservations(List.of(obs1, obs2));

        ReportTestData test = new ReportTestData();
        test.setName("Test presión");
        test.setStatus("COMPLETED");
        test.setDescription("Verificar manómetro");
        test.setCreatedAt(Instant.parse("2025-01-15T11:00:00Z"));
        test.setSteps(List.of(step));

        ReportDeviceData dev = new ReportDeviceData();
        dev.setName("Extintor #1");
        dev.setDeviceCategory("PQS");
        dev.setDeviceSerialNumber(12345);
        dev.setTests(List.of(test));

        ReportZoneData zone = new ReportZoneData();
        zone.setName("Sótano");
        zone.setDevices(List.of(dev));

        ReportLocationData loc = new ReportLocationData();
        loc.setName("Ubicación A");
        loc.setDetails("Detalle ubicación");
        loc.setZones(List.of(zone));
        return loc;
    }
}
