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
    private final UserRepository userRepository;

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
                            InspectionAssignmentRepository assignmentRepository,
                            UserRepository userRepository) {
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
        this.userRepository = userRepository;
    }

    /**
     * Obtiene las inspecciones según el rol del usuario autenticado.
     * - ADMIN: ve todas las inspecciones.
     * - INSPECTOR, OPERATOR: solo las inspecciones donde está asignado.
     *
     * @param userEmail Email del usuario autenticado (no null para INSPECTOR/OPERATOR)
     * @param userRole  Rol del usuario: ADMIN, INSPECTOR u OPERATOR
     * @return Lista filtrada de InspectionListResponse
     */
    public List<InspectionListResponse> getInspectionsForCurrentUser(String userEmail, String userRole) {
        String role = (userRole != null ? userRole : "").toUpperCase();
        if ("ADMIN".equals(role)) {
            return inspectionRepository.findAll().stream()
                    .map(this::mapToListResponse)
                    .collect(Collectors.toList());
        }
        // INSPECTOR (o rol desconocido): solo inspecciones asignadas
        String email = (userEmail != null ? userEmail : "").trim().toLowerCase();
        if (email.isEmpty()) {
            return List.of();
        }
        List<InspectionAssignment> assignments = assignmentRepository.findByUserEmailIgnoreCase(email);
        if (assignments.isEmpty()) {
            return List.of();
        }
        java.util.Set<String> inspectionIds = assignments.stream()
                .map(InspectionAssignment::getInspectionId)
                .collect(Collectors.toSet());
        return inspectionRepository.findAllById(inspectionIds).stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea una inspección building-wide con snapshot de tests heredados.
     *
     * @param creatorEmail email del usuario autenticado (se guarda en {@code createdByEmail})
     * @param creatorRole  rol del creador (ADMIN: usa asignaciones del body; INSPECTOR: fuerza inspector = creador y solo operadores del body)
     */
    @Transactional
    public CreateInspectionResponse createInspection(CreateInspectionRequest request,
                                                     String creatorEmail,
                                                     String creatorRole) {
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

        String createdBy = creatorEmail == null ? "" : creatorEmail.trim().toLowerCase();
        if (createdBy.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        List<AssignmentRequest> assignmentsToPersist = resolveAssignmentsForCreate(request, createdBy, creatorRole);

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
        inspection.setCreatedByEmail(createdBy);
        inspection.setCreatedAt(now);
        inspection.setUpdatedAt(now);
        inspectionRepository.save(inspection);

        int testsCreated = createSnapshotTests(inspectionId, buildingId, now);
        createAssignments(inspectionId, assignmentsToPersist, now);

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

    /**
     * Inicia una inspección: transiciona de PENDING/SCHEDULED a IN_PROGRESS.
     * Solo se permite si la inspección tiene al menos un INSPECTOR asignado.
     */
    @Transactional
    public InspectionListResponse startInspection(String inspectionId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inspección no encontrada: " + inspectionId));

        if ("IN_PROGRESS".equals(inspection.getStatus())) {
            return mapToListResponse(inspection);
        }

        if (inspection.getStatus() != null && inspection.getStatus().startsWith("DONE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede iniciar una inspección ya finalizada.");
        }

        List<InspectionAssignment> inspectors = assignmentRepository
                .findByInspectionId(inspectionId).stream()
                .filter(a -> ROLE_INSPECTOR.equals(a.getRole()))
                .collect(Collectors.toList());
        if (inspectors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Se requiere al menos 1 Inspector asignado para iniciar la inspección.");
        }

        inspection.setStatus("IN_PROGRESS");
        inspection.setUpdatedAt(Instant.now());
        inspectionRepository.save(inspection);

        return mapToListResponse(inspection);
    }

    /**
     * Firma digitalmente una inspección.
     *
     * Validaciones:
     * 1. La inspección debe existir.
     * 2. La inspección no puede estar ya firmada.
     * 3. La inspección debe estar IN_PROGRESS (no PENDING ni ya DONE).
     * 4. El usuario que firma debe ser el INSPECTOR asignado a esta inspección.
     * 5. Todos los tests de la inspección deben tener status COMPLETED o FAILED
     *    (ninguno puede quedar PENDING).
     *
     * Al firmar:
     * - signer = nombre del firmante (signerName del request).
     * - signed = true
     * - signDate = ahora
     * - status = DONE_COMPLETED si todos los tests son COMPLETED;
     *            DONE_FAILED si al menos uno es FAILED.
     * - result = SUCCESS o FAILED (según tests).
     */
    @Transactional
    public InspectionListResponse signInspection(String inspectionId,
                                                  String signerName,
                                                  String userEmail) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inspección no encontrada: " + inspectionId));

        if (inspection.isSigned()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La inspección ya fue firmada por " + inspection.getSigner());
        }

        if (!"IN_PROGRESS".equals(inspection.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden firmar inspecciones en estado IN_PROGRESS. Estado actual: "
                            + inspection.getStatus());
        }

        // Verificar que el usuario es el INSPECTOR asignado
        List<InspectionAssignment> inspectorAssignments =
                assignmentRepository.findByInspectionIdAndRole(inspectionId, ROLE_INSPECTOR);
        boolean isAssignedInspector = inspectorAssignments.stream()
                .anyMatch(a -> a.getUserEmail().equalsIgnoreCase(userEmail));
        if (!isAssignedInspector) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el Inspector asignado puede firmar esta inspección.");
        }

        // Verificar que todos los tests estén COMPLETED o FAILED
        List<InspectionTest> tests = testRepository.findByInspectionId(inspectionId);
        if (tests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La inspección no tiene tests. No se puede firmar.");
        }

        boolean anyPending = false;
        boolean anyFailed  = false;
        for (InspectionTest test : tests) {
            String st = test.getStatus();
            if ("PENDING".equals(st)) {
                anyPending = true;
            } else if ("FAILED".equals(st)) {
                anyFailed = true;
            }
        }
        if (anyPending) {
            long pendingCount = tests.stream()
                    .filter(t -> "PENDING".equals(t.getStatus())).count();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede firmar: " + pendingCount + " test(s) aún están PENDIENTES. "
                            + "Todos los tests deben estar COMPLETED o FAILED.");
        }

        // Aplicar firma
        Instant now = Instant.now();
        inspection.setSigner(signerName);
        inspection.setSigned(true);
        inspection.setSignDate(now);
        inspection.setStatus(anyFailed ? "DONE_FAILED" : "DONE_COMPLETED");
        inspection.setResult(anyFailed ? "FAILED" : "SUCCESS");
        inspection.setUpdatedAt(now);
        inspectionRepository.save(inspection);

        return mapToListResponse(inspection);
    }

    /**
     * Obtiene el status actual de una inspección, recalculándolo desde sus tests.
     * Devuelve un InspectionListResponse con el estado actualizado.
     */
    public InspectionListResponse getInspectionStatus(String inspectionId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inspección no encontrada: " + inspectionId));
        return mapToListResponse(inspection);
    }

    /**
     * Recalcula forzadamente el estado de una inspección a partir de sus tests.
     * Útil cuando se quiere sincronizar el estado sin pasar por un step update.
     */
    @Transactional
    public InspectionListResponse recalculateAndGetStatus(String inspectionId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Inspección no encontrada: " + inspectionId));

        if (!"IN_PROGRESS".equals(inspection.getStatus())) {
            return mapToListResponse(inspection);
        }

        List<InspectionTest> tests = testRepository.findByInspectionId(inspectionId);
        if (tests.isEmpty()) {
            return mapToListResponse(inspection);
        }

        boolean anyPending = false;
        boolean anyFailed  = false;
        for (InspectionTest t : tests) {
            String st = t.getStatus() != null ? t.getStatus() : "PENDING";
            if ("SUCCESS".equals(st)) st = "COMPLETED";
            if ("PENDING".equals(st)) { anyPending = true; break; }
            if ("FAILED".equals(st)) anyFailed = true;
        }

        // Do NOT persist DONE_* here. Inspection stays IN_PROGRESS until signInspection.
        // T5.3.3: only signInspection may transition to DONE_COMPLETED / DONE_FAILED.
        return mapToListResponse(inspection);
    }
    /**
     * ADMIN: valida y usa las asignaciones del request tal cual.
     * INSPECTOR: un único inspector (el creador) y operadores opcionales deduplicados del body.
     */
    private List<AssignmentRequest> resolveAssignmentsForCreate(CreateInspectionRequest request,
                                                                  String creatorEmailNormalized,
                                                                  String creatorRole) {
        String upperRole = (creatorRole != null ? creatorRole : "").toUpperCase();
        if ("INSPECTOR".equals(upperRole)) {
            List<AssignmentRequest> merged = new ArrayList<>();
            LinkedHashSet<String> seenOperatorEmails = new LinkedHashSet<>();
            if (request.getAssignments() != null) {
                for (AssignmentRequest a : request.getAssignments()) {
                    if (a == null) continue;
                    String r = (a.getRole() != null ? a.getRole().trim() : "").toUpperCase();
                    if (!ROLE_OPERATOR.equals(r)) {
                        continue;
                    }
                    String em = (a.getUserEmail() != null ? a.getUserEmail().trim() : "").toLowerCase();
                    if (em.isEmpty() || em.equals(creatorEmailNormalized)) {
                        continue;
                    }
                    if (seenOperatorEmails.add(em)) {
                        merged.add(new AssignmentRequest(em, ROLE_OPERATOR));
                    }
                }
            }
            merged.add(new AssignmentRequest(creatorEmailNormalized, ROLE_INSPECTOR));
            validateAssignments(merged);
            return merged;
        }
        if ("ADMIN".equals(upperRole)) {
            List<AssignmentRequest> adminAssignments = request.getAssignments() != null
                    ? request.getAssignments()
                    : List.of();
            validateAssignments(adminAssignments);
            return new ArrayList<>(adminAssignments);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Solo ADMIN o INSPECTOR pueden crear inspecciones");
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
                User user = userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El usuario no existe: " + email));
                String accountRole = user.getRole() != null ? user.getRole().trim().toUpperCase() : "";
                if (!ROLE_INSPECTOR.equals(accountRole)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "El usuario no tiene rol de Inspector");
                }
            } else if (ROLE_OPERATOR.equals(role)) {
                userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El usuario no existe: " + email));
            }
            validateUserRoleMatchesAssignment(email, role);
        }
        if (inspectorCount < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one INSPECTOR is required");
        }
        if (inspectorCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only one INSPECTOR is allowed per inspection");
        }
    }

    private void validateUserRoleMatchesAssignment(String emailNormalized, String assignmentRoleUpper) {
        User user = userRepository.findByEmailIgnoreCase(emailNormalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Usuario no registrado: " + emailNormalized));
        String dbRole = user.getRole() != null ? user.getRole().toUpperCase() : "";
        if (ROLE_INSPECTOR.equals(assignmentRoleUpper)) {
            if (!ROLE_INSPECTOR.equals(dbRole)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo usuarios con rol INSPECTOR pueden asignarse como inspector");
            }
        } else if (ROLE_OPERATOR.equals(assignmentRoleUpper)) {
            if (!ROLE_INSPECTOR.equals(dbRole) && !ROLE_OPERATOR.equals(dbRole)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "En operadores solo se pueden asignar usuarios con rol INSPECTOR u OPERATOR");
            }
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
        InspectionListResponse dto = new InspectionListResponse(
                inspection.getId(),
                inspection.getBuildingId(),
                buildingName,
                inspection.getLocationId(),
                inspection.getStatus(),
                inspection.getScheduledDate(),
                inspection.getType(),
                inspection.getResult()
        );
        dto.setSigner(inspection.getSigner());
        dto.setSigned(inspection.isSigned());
        dto.setSignDate(inspection.getSignDate());
        dto.setCreatedByEmail(inspection.getCreatedByEmail());
        return dto;
    }
}
