package com.inspections.service;

import com.inspections.dto.report.*;
import com.inspections.entity.*;
import com.inspections.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio que agrega todos los datos necesarios para el reporte PDF de una inspección.
 * Valida que la inspección esté firmada y cerrada, y que el usuario sea inspector u operador asignado.
 */
@Service
public class InspectionReportService {

    private static final String ROLE_INSPECTOR = "INSPECTOR";
    private static final String ROLE_OPERATOR = "OPERATOR";
    private static final String STATUS_DONE_COMPLETED = "DONE_COMPLETED";
    private static final String STATUS_DONE_FAILED = "DONE_FAILED";
    private static final String TYPE_DEFICIENCIES = "DEFICIENCIES";

    private final InspectionRepository inspectionRepository;
    private final BuildingRepository buildingRepository;
    private final InspectionAssignmentRepository assignmentRepository;
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final TestRepository testRepository;
    private final StepRepository stepRepository;
    private final ObservationRepository observationRepository;
    private final PhotoRepository photoRepository;
    private final DeficiencyTypeRepository deficiencyTypeRepository;

    public InspectionReportService(InspectionRepository inspectionRepository,
                                   BuildingRepository buildingRepository,
                                   InspectionAssignmentRepository assignmentRepository,
                                   LocationRepository locationRepository,
                                   ZoneRepository zoneRepository,
                                   DeviceRepository deviceRepository,
                                   TestRepository testRepository,
                                   StepRepository stepRepository,
                                   ObservationRepository observationRepository,
                                   PhotoRepository photoRepository,
                                   DeficiencyTypeRepository deficiencyTypeRepository) {
        this.inspectionRepository = inspectionRepository;
        this.buildingRepository = buildingRepository;
        this.assignmentRepository = assignmentRepository;
        this.locationRepository = locationRepository;
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.testRepository = testRepository;
        this.stepRepository = stepRepository;
        this.observationRepository = observationRepository;
        this.photoRepository = photoRepository;
        this.deficiencyTypeRepository = deficiencyTypeRepository;
    }

    /**
     * Construye el árbol completo de datos para el reporte.
     *
     * @param inspectionId ID de la inspección
     * @param userEmail    Email del usuario autenticado (debe ser inspector u operador asignado)
     * @return InspectionReportData con toda la jerarquía
     */
    public InspectionReportData buildReportData(String inspectionId, String userEmail) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inspección no encontrada: " + inspectionId));

        if (!inspection.isSigned()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden generar reportes de inspecciones firmadas.");
        }
        if (!STATUS_DONE_COMPLETED.equals(inspection.getStatus())
                && !STATUS_DONE_FAILED.equals(inspection.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden generar reportes de inspecciones finalizadas.");
        }

        List<InspectionAssignment> assignments = assignmentRepository.findByInspectionId(inspectionId);
        boolean isAssigned = assignments.stream()
                .filter(a -> ROLE_INSPECTOR.equals(a.getRole()) || ROLE_OPERATOR.equals(a.getRole()))
                .anyMatch(a -> a.getUserEmail() != null && a.getUserEmail().equalsIgnoreCase(userEmail));
        if (!isAssigned) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo inspectores y operadores asignados pueden descargar el reporte.");
        }

        InspectionReportData data = new InspectionReportData();
        data.setInspectionId(inspection.getId());
        data.setBuildingId(inspection.getBuildingId());
        data.setType(inspection.getType());
        data.setStatus(inspection.getStatus());
        data.setResult(inspection.getResult());
        data.setNotes(inspection.getNotes());
        data.setScheduledDate(inspection.getScheduledDate());
        data.setStartedAt(inspection.getStartedAt());
        data.setSignDate(inspection.getSignDate());
        data.setSigner(inspection.getSigner());
        data.setSigned(inspection.isSigned());

        String buildingName = "";
        if (inspection.getBuildingId() != null && !inspection.getBuildingId().isBlank()) {
            buildingName = buildingRepository.findById(inspection.getBuildingId())
                    .map(Building::getName)
                    .orElse(inspection.getBuildingId());
        }
        data.setBuildingName(buildingName);

        List<String> inspectorEmails = assignments.stream()
                .filter(a -> ROLE_INSPECTOR.equals(a.getRole()))
                .map(InspectionAssignment::getUserEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        data.setInspectorEmails(inspectorEmails);

        List<String> operatorEmails = assignments.stream()
                .filter(a -> ROLE_OPERATOR.equals(a.getRole()))
                .map(InspectionAssignment::getUserEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        data.setOperatorEmails(operatorEmails);

        List<Observation> allObservations = observationRepository.findByInspectionId(inspectionId);
        Map<String, List<Observation>> observationsByStepId = allObservations.stream()
                .collect(Collectors.groupingBy(Observation::getTestStepId));

        Map<String, String> deficiencyTypeNames = new HashMap<>();
        for (DeficiencyType dt : deficiencyTypeRepository.findAll()) {
            deficiencyTypeNames.put(dt.getId(), dt.getName());
        }

        List<ReportLocationData> locations = new ArrayList<>();
        if (inspection.getBuildingId() != null) {
            List<Location> locationList = locationRepository.findByBuildingIdOrderByNameAsc(inspection.getBuildingId());
            for (Location loc : locationList) {
                ReportLocationData locData = new ReportLocationData();
                locData.setId(loc.getId());
                locData.setName(loc.getName());
                locData.setDetails(loc.getDetails());

                List<ReportZoneData> zones = new ArrayList<>();
                for (Zone zone : zoneRepository.findByLocationIdOrderByNameAsc(loc.getId())) {
                    ReportZoneData zoneData = new ReportZoneData();
                    zoneData.setId(zone.getId());
                    zoneData.setName(zone.getName());
                    zoneData.setDetails(zone.getDetails());

                    List<ReportDeviceData> devices = new ArrayList<>();
                    for (Device dev : deviceRepository.findByZoneId(zone.getId())) {
                        ReportDeviceData devData = new ReportDeviceData();
                        devData.setId(dev.getId());
                        devData.setName(dev.getName());
                        devData.setDescription(dev.getDescription());
                        devData.setDeviceCategory(dev.getDeviceCategory());
                        devData.setDeviceSerialNumber(dev.getDeviceSerialNumber());

                        List<ReportTestData> tests = new ArrayList<>();
                        for (InspectionTest test : testRepository.findByDeviceIdAndInspectionId(dev.getId(), inspectionId)) {
                            ReportTestData testData = new ReportTestData();
                            testData.setId(test.getId());
                            testData.setName(test.getName());
                            testData.setDescription(test.getDescription());
                            testData.setStatus(test.getStatus());
                            testData.setCreatedAt(test.getCreatedAt());

                            List<ReportStepData> steps = new ArrayList<>();
                            for (Step step : stepRepository.findByTestIdOrderByCreatedAtAsc(test.getId())) {
                                ReportStepData stepData = new ReportStepData();
                                stepData.setId(step.getId());
                                stepData.setName(step.getName());
                                stepData.setTestStepType(step.getTestStepType());
                                stepData.setApplicable(step.isApplicable());
                                stepData.setStatus(step.getStatus());
                                stepData.setDescription(step.getDescription());
                                stepData.setValueJson(step.getValueJson());
                                stepData.setMinValue(step.getMinValue());
                                stepData.setMaxValue(step.getMaxValue());
                                stepData.setCreatedAt(step.getCreatedAt());

                                List<ReportObservationData> stepObs = new ArrayList<>();
                                for (Observation obs : observationsByStepId.getOrDefault(step.getId(), List.of())) {
                                    ReportObservationData obsData = new ReportObservationData();
                                    obsData.setId(obs.getId());
                                    obsData.setName(obs.getName());
                                    obsData.setType(obs.getType());
                                    obsData.setDescription(obs.getDescription());
                                    obsData.setDeficiencyTypeId(obs.getDeficiencyTypeId());
                                    obsData.setDeficiencyTypeName(deficiencyTypeNames.get(obs.getDeficiencyTypeId()));
                                    obsData.setMediaId(obs.getMediaId());
                                    obsData.setCreatedAt(obs.getCreatedAt());
                                    if (obs.getMediaId() != null) {
                                        photoRepository.findById(obs.getMediaId()).ifPresent(photo -> {
                                            obsData.setMediaUrl(photo.getMediaUrl());
                                            obsData.setPhotoMetadata(buildPhotoMetadata(photo));
                                        });
                                    }
                                    stepObs.add(obsData);
                                }
                                stepData.setObservations(stepObs);
                                steps.add(stepData);
                            }
                            testData.setSteps(steps);
                            tests.add(testData);
                        }
                        devData.setTests(tests);
                        devices.add(devData);
                    }
                    zoneData.setDevices(devices);
                    zones.add(zoneData);
                }
                locData.setZones(zones);
                locations.add(locData);
            }
        }
        data.setLocations(locations);

        int locCount = locations.size();
        int zoneCount = locations.stream().mapToInt(l -> l.getZones().size()).sum();
        int deviceCount = locations.stream()
                .flatMap(l -> l.getZones().stream())
                .mapToInt(z -> z.getDevices().size()).sum();
        int testCount = locations.stream()
                .flatMap(l -> l.getZones().stream())
                .flatMap(z -> z.getDevices().stream())
                .mapToInt(d -> d.getTests().size()).sum();
        int stepCount = locations.stream()
                .flatMap(l -> l.getZones().stream())
                .flatMap(z -> z.getDevices().stream())
                .flatMap(d -> d.getTests().stream())
                .mapToInt(t -> t.getSteps().size()).sum();
        data.setLocationCount(locCount);
        data.setZoneCount(zoneCount);
        data.setDeviceCount(deviceCount);
        data.setTestCount(testCount);
        data.setStepCount(stepCount);
        data.setObservationCount(allObservations.size());
        data.setDeficiencyCount((int) allObservations.stream()
                .filter(o -> TYPE_DEFICIENCIES.equals(o.getType()))
                .count());
        data.setPhotoCount((int) allObservations.stream()
                .filter(o -> o.getMediaId() != null && !o.getMediaId().isBlank())
                .count());

        return data;
    }

    private String buildPhotoMetadata(Photo photo) {
        List<String> parts = new ArrayList<>();
        if (photo.getTimestamp() != null) {
            parts.add("Fecha: " + photo.getTimestamp().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (photo.getInspectorId() != null) {
            parts.add("Inspector ID: " + photo.getInspectorId());
        }
        if (photo.getMediaUrl() != null) {
            parts.add("URL: " + photo.getMediaUrl());
        }
        if (photo.getLocalPath() != null) {
            parts.add("Ruta local: " + photo.getLocalPath());
        }
        return parts.isEmpty() ? "" : String.join(" | ", parts);
    }
}
