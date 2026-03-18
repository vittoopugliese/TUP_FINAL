package com.inspections.service;

import com.inspections.dto.AssignmentRequest;
import com.inspections.dto.CreateInspectionRequest;
import com.inspections.dto.CreateInspectionResponse;
import com.inspections.dto.InspectionListResponse;
import com.inspections.entity.*;
import com.inspections.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones de inspecciones.
 */
@Service
public class InspectionService {

    private static final Set<String> VALID_TYPES = Set.of("Daily", "Weekly", "Monthly", "Annually");
    private static final String ROLE_INSPECTOR = "INSPECTOR";
    private static final String ROLE_OPERATOR = "OPERATOR";

    private final InspectionRepository inspectionRepository;
    private final BuildingRepository buildingRepository;
    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceTypeTestTemplateRepository deviceTypeTestTemplateRepository;
    private final TestTemplateRepository testTemplateRepository;
    private final TestRepository testRepository;
    private final TestTemplateStepRepository testTemplateStepRepository;
    private final StepRepository stepRepository;
    private final InspectionAssignmentRepository assignmentRepository;

    public InspectionService(InspectionRepository inspectionRepository,
                            BuildingRepository buildingRepository,
                            InspectionTemplateRepository inspectionTemplateRepository,
                            LocationRepository locationRepository,
                            ZoneRepository zoneRepository,
                            DeviceRepository deviceRepository,
                            DeviceTypeTestTemplateRepository deviceTypeTestTemplateRepository,
                            TestTemplateRepository testTemplateRepository,
                            TestRepository testRepository,
                            TestTemplateStepRepository testTemplateStepRepository,
                            StepRepository stepRepository,
                            InspectionAssignmentRepository assignmentRepository) {
        this.inspectionRepository = inspectionRepository;
        this.buildingRepository = buildingRepository;
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.locationRepository = locationRepository;
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTypeTestTemplateRepository = deviceTypeTestTemplateRepository;
        this.testTemplateRepository = testTemplateRepository;
        this.testRepository = testRepository;
        this.testTemplateStepRepository = testTemplateStepRepository;
        this.stepRepository = stepRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Obtiene todas las inspecciones para la lista.
     *
     * @return Lista de InspectionListResponse
     */
    public List<InspectionListResponse> getAllInspections() {
        return inspectionRepository.findAll().stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea una inspección building-wide con snapshot de tests heredados.
     */
    @Transactional
    public CreateInspectionResponse createInspection(CreateInspectionRequest request) {
        String buildingId = request.getBuildingId().trim();
        String type = request.getType().trim();
        String inspectionTemplateId = request.getInspectionTemplateId().trim();

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found: " + buildingId));

        if (!VALID_TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid type. Must be one of: Daily, Weekly, Monthly, Annually");
        }

        inspectionTemplateRepository.findById(inspectionTemplateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Inspection template not found: " + inspectionTemplateId));

        validateAssignments(request.getAssignments());

        Instant now = Instant.now();
        String inspectionId = UUID.randomUUID().toString();

        Inspection inspection = new Inspection();
        inspection.setId(inspectionId);
        inspection.setBuildingId(buildingId);
        inspection.setLocationId(null); // building-wide
        inspection.setType(type);
        inspection.setStatus("PENDING");
        inspection.setScheduledDate(request.getScheduledDate());
        inspection.setInspectionTemplateId(inspectionTemplateId);
        inspection.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        inspection.setCreatedAt(now);
        inspection.setUpdatedAt(now);
        inspectionRepository.save(inspection);

        int testsCreated = createSnapshotTests(inspectionId, buildingId, now);
        createAssignments(inspectionId, request.getAssignments(), now);

        List<Location> locations = locationRepository.findByBuildingIdOrderByNameAsc(buildingId);
        int zonesCount = 0;
        for (Location loc : locations) {
            zonesCount += zoneRepository.findByLocationId(loc.getId()).size();
        }
        int devicesCount = deviceRepository.findByBuildingId(buildingId).size();

        return new CreateInspectionResponse(
                inspectionId,
                buildingId,
                building.getName(),
                type,
                "PENDING",
                request.getScheduledDate(),
                inspectionTemplateId,
                inspection.getNotes(),
                locations.size(),
                zonesCount,
                devicesCount,
                testsCreated
        );
    }

    private void validateAssignments(List<AssignmentRequest> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one INSPECTOR is required");
        }
        Set<String> emails = new HashSet<>();
        long inspectorCount = 0;
        for (AssignmentRequest a : assignments) {
            String email = (a.getUserEmail() != null ? a.getUserEmail().trim() : "").toLowerCase();
            String role = (a.getRole() != null ? a.getRole().trim() : "").toUpperCase();
            if (email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment email cannot be blank");
            }
            if (!ROLE_INSPECTOR.equals(role) && !ROLE_OPERATOR.equals(role)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be INSPECTOR or OPERATOR");
            }
            if (emails.contains(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate email in assignments: " + email);
            }
            emails.add(email);
            if (ROLE_INSPECTOR.equals(role)) {
                inspectorCount++;
            }
        }
        if (inspectorCount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one INSPECTOR is required");
        }
        if (inspectorCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one INSPECTOR is allowed per inspection");
        }
    }

    private int createSnapshotTests(String inspectionId, String buildingId, Instant now) {
        List<Device> devices = deviceRepository.findByBuildingId(buildingId);
        int count = 0;
        for (Device device : devices) {
            if (!device.isEnabled()) continue;
            var mappings = deviceTypeTestTemplateRepository.findByDeviceTypeIdOrderBySortOrderAsc(device.getDeviceTypeId());
            for (var mapping : mappings) {
                if (testRepository.existsByDeviceIdAndInspectionIdAndTestTemplateId(
                        device.getId(), inspectionId, mapping.getTestTemplateId())) {
                    continue;
                }
                TestTemplate template = testTemplateRepository.findById(mapping.getTestTemplateId()).orElse(null);
                if (template == null || !template.isEnabled()) continue;

                InspectionTest test = new InspectionTest();
                test.setId(UUID.randomUUID().toString());
                test.setDeviceId(device.getId());
                test.setInspectionId(inspectionId);
                test.setTestTemplateId(template.getId());
                test.setName(template.getName());
                test.setDescription(template.getDescription());
                test.setStatus("PENDING");
                test.setApplicable(true);
                test.setCreatedAt(now);
                test.setUpdatedAt(now);
                testRepository.save(test);
                cloneTemplateStepsToTest(test.getId(), template.getId(), now);
                count++;
            }
        }
        return count;
    }

    private void cloneTemplateStepsToTest(String testId, String templateId, Instant now) {
        List<TestTemplateStep> templateSteps = testTemplateStepRepository.findByTestTemplateIdOrderBySortOrderAsc(templateId);
        for (TestTemplateStep tts : templateSteps) {
            Step step = new Step();
            step.setId(UUID.randomUUID().toString());
            step.setTestId(testId);
            step.setName(tts.getName());
            step.setTestStepType(tts.getTestStepType());
            step.setApplicable(true);
            step.setStatus("PENDING");
            step.setDescription(tts.getDescription());
            step.setValueJson(null);
            step.setMinValue(tts.getMinValue());
            step.setMaxValue(tts.getMaxValue());
            step.setCreatedAt(now);
            step.setUpdatedAt(now);
            stepRepository.save(step);
        }
    }

    private void createAssignments(String inspectionId, List<AssignmentRequest> assignments, Instant now) {
        for (AssignmentRequest a : assignments) {
            String email = a.getUserEmail().trim().toLowerCase();
            String role = a.getRole().trim().toUpperCase();
            InspectionAssignment assignment = new InspectionAssignment();
            assignment.setId(UUID.randomUUID().toString());
            assignment.setInspectionId(inspectionId);
            assignment.setUserEmail(email);
            assignment.setRole(role);
            assignment.setCreatedAt(now);
            assignmentRepository.save(assignment);
        }
    }

    private InspectionListResponse mapToListResponse(Inspection inspection) {
        String buildingName = null;
        if (inspection.getBuildingId() != null && !inspection.getBuildingId().isBlank()) {
            buildingName = buildingRepository.findById(inspection.getBuildingId())
                    .map(Building::getName)
                    .orElse(null);
        }
        return new InspectionListResponse(
                inspection.getId(),
                inspection.getBuildingId(),
                buildingName,
                inspection.getLocationId(),
                inspection.getStatus(),
                inspection.getScheduledDate(),
                inspection.getType()
        );
    }
}
