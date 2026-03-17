package com.inspections.service;

import com.inspections.dto.CreateDeviceRequest;
import com.inspections.dto.DeviceWithTestsResponse;
import com.inspections.dto.MoveDeviceRequest;
import com.inspections.dto.MoveDeviceResponse;
import com.inspections.dto.TestResponse;
import com.inspections.entity.Device;
import com.inspections.entity.DeviceType;
import com.inspections.entity.Inspection;
import com.inspections.entity.InspectionTest;
import com.inspections.entity.Location;
import com.inspections.entity.TestTemplate;
import com.inspections.entity.Zone;
import com.inspections.repository.DeviceRepository;
import com.inspections.repository.DeviceTypeTestTemplateRepository;
import com.inspections.repository.DeviceTypeRepository;
import com.inspections.repository.InspectionRepository;
import com.inspections.repository.LocationRepository;
import com.inspections.repository.TestRepository;
import com.inspections.repository.TestTemplateRepository;
import com.inspections.repository.ZoneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones CRUD de dispositivos.
 */
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceTypeTestTemplateRepository deviceTypeTestTemplateRepository;
    private final TestTemplateRepository testTemplateRepository;
    private final TestRepository testRepository;
    private final InspectionRepository inspectionRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;

    public DeviceService(DeviceRepository deviceRepository,
                         DeviceTypeRepository deviceTypeRepository,
                         DeviceTypeTestTemplateRepository deviceTypeTestTemplateRepository,
                         TestTemplateRepository testTemplateRepository,
                         TestRepository testRepository,
                         InspectionRepository inspectionRepository,
                         ZoneRepository zoneRepository,
                         LocationRepository locationRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceTypeRepository = deviceTypeRepository;
        this.deviceTypeTestTemplateRepository = deviceTypeTestTemplateRepository;
        this.testTemplateRepository = testTemplateRepository;
        this.testRepository = testRepository;
        this.inspectionRepository = inspectionRepository;
        this.zoneRepository = zoneRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Crea un dispositivo en la zona indicada.
     * Si inspectionId está presente, crea tests heredados para esa inspección.
     */
    @Transactional
    public DeviceWithTestsResponse createDevice(String locationId, String zoneId, CreateDeviceRequest request) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zone not found: " + zoneId));

        if (!zone.getLocationId().equals(locationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Zone " + zoneId + " does not belong to location " + locationId);
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + locationId));

        DeviceType deviceType = deviceTypeRepository.findById(request.getDeviceTypeId().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device type not found: " + request.getDeviceTypeId()));

        if (!deviceType.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Device type is disabled: " + request.getDeviceTypeId());
        }

        String category = request.getDeviceCategory() != null && !request.getDeviceCategory().isBlank()
                ? request.getDeviceCategory().trim()
                : deviceType.getCategory();

        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.setZoneId(zoneId);
        device.setLocationId(locationId);
        device.setBuildingId(location.getBuildingId());
        device.setDeviceTypeId(deviceType.getId());
        device.setName(request.getName().trim());
        device.setDeviceCategory(category);
        device.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        device.setDeviceSerialNumber(request.getSerialNumber());
        device.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        device.setCreatedAt(Instant.now());
        device.setUpdatedAt(Instant.now());

        device = deviceRepository.save(device);

        List<TestResponse> createdTests = Collections.emptyList();
        String inspectionId = request.getInspectionId() != null ? request.getInspectionId().trim() : null;
        if (inspectionId != null && !inspectionId.isBlank()) {
            createdTests = createInheritedTests(device.getId(), deviceType.getId(), inspectionId, locationId);
        }

        return new DeviceWithTestsResponse(
                device.getId(),
                device.getZoneId(),
                device.getLocationId(),
                device.getName(),
                device.getDeviceCategory(),
                device.getDeviceSerialNumber(),
                device.isEnabled(),
                createdTests
        );
    }

    /**
     * Crea tests heredados para device+inspection según templates del device type.
     * Idempotente: no duplica si ya existe (device, inspection, template).
     */
    private List<TestResponse> createInheritedTests(String deviceId, String deviceTypeId,
                                                    String inspectionId, String locationId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Inspection not found: " + inspectionId));

        if (inspection.getLocationId() != null && !locationId.equals(inspection.getLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Inspection " + inspectionId + " does not belong to location " + locationId);
        }

        var mappings = deviceTypeTestTemplateRepository.findByDeviceTypeIdOrderBySortOrderAsc(deviceTypeId);
        if (mappings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Device type has no test templates configured. Cannot create inherited tests.");
        }

        List<TestResponse> created = new ArrayList<>();
        Instant now = Instant.now();

        for (var mapping : mappings) {
            if (testRepository.existsByDeviceIdAndInspectionIdAndTestTemplateId(
                    deviceId, inspectionId, mapping.getTestTemplateId())) {
                continue; // idempotency: skip if already exists
            }

            TestTemplate template = testTemplateRepository.findById(mapping.getTestTemplateId())
                    .orElse(null);
            if (template == null || !template.isEnabled()) {
                continue;
            }

            InspectionTest test = new InspectionTest();
            test.setId(UUID.randomUUID().toString());
            test.setDeviceId(deviceId);
            test.setInspectionId(inspectionId);
            test.setTestTemplateId(template.getId());
            test.setName(template.getName());
            test.setDescription(template.getDescription());
            test.setStatus("PENDING");
            test.setApplicable(true);
            test.setCreatedAt(now);
            test.setUpdatedAt(now);
            testRepository.save(test);

            created.add(new TestResponse(
                    test.getId(), test.getDeviceId(), test.getInspectionId(),
                    test.getName(), test.getDescription(), test.getStatus()));
        }

        return created;
    }

    /**
     * Mueve un dispositivo a otra zona dentro de la misma location.
     * Los tests asociados se mantienen intactos.
     */
    public MoveDeviceResponse moveDeviceWithinLocation(String locationId, String deviceId,
                                                       MoveDeviceRequest request) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found: " + deviceId));

        if (!locationId.equals(device.getLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Device " + deviceId + " does not belong to location " + locationId);
        }

        String targetZoneId = request.getTargetZoneId().trim();
        if (targetZoneId.equals(device.getZoneId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Target zone must differ from current zone");
        }

        Zone targetZone = zoneRepository.findById(targetZoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zone not found: " + targetZoneId));

        if (!targetZone.getLocationId().equals(locationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Target zone " + targetZoneId + " does not belong to location " + locationId);
        }

        String oldZoneId = device.getZoneId();
        device.setZoneId(targetZoneId);
        device.setUpdatedAt(Instant.now());
        deviceRepository.save(device);

        return new MoveDeviceResponse(
                device.getId(),
                oldZoneId,
                targetZoneId,
                locationId,
                device.getUpdatedAt()
        );
    }
}
