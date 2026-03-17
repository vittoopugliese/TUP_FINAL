package com.inspections.service;

import com.inspections.dto.CreateZoneRequest;
import com.inspections.dto.DeviceWithTestsResponse;
import com.inspections.dto.TestResponse;
import com.inspections.dto.ZoneWithDevicesResponse;
import com.inspections.entity.Device;
import com.inspections.entity.InspectionTest;
import com.inspections.entity.Zone;
import com.inspections.repository.DeviceRepository;
import com.inspections.repository.LocationRepository;
import com.inspections.repository.TestRepository;
import com.inspections.repository.ZoneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para construir la jerarquía Zona -> Devices -> Tests.
 */
@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final TestRepository testRepository;
    private final LocationRepository locationRepository;

    public ZoneService(ZoneRepository zoneRepository,
                       DeviceRepository deviceRepository,
                       TestRepository testRepository,
                       LocationRepository locationRepository) {
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.testRepository = testRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Obtiene zonas con sus devices y tests para una ubicación e inspección.
     */
    public List<ZoneWithDevicesResponse> getZonesWithDevicesAndTests(
            String locationId, String inspectionId) {

        List<Zone> zones = zoneRepository.findByLocationIdOrderByNameAsc(locationId);

        return zones.stream().map(zone -> {
            List<Device> devices = deviceRepository.findByZoneId(zone.getId());

            List<DeviceWithTestsResponse> deviceResponses = devices.stream().map(device -> {
                List<InspectionTest> tests = (inspectionId != null && !inspectionId.isBlank())
                        ? testRepository.findByDeviceIdAndInspectionId(device.getId(), inspectionId)
                        : testRepository.findByDeviceId(device.getId());

                List<TestResponse> testResponses = tests.stream()
                        .map(t -> new TestResponse(
                                t.getId(), t.getDeviceId(), t.getInspectionId(),
                                t.getName(), t.getDescription(), t.getStatus()))
                        .collect(Collectors.toList());

                return new DeviceWithTestsResponse(
                        device.getId(), device.getZoneId(), device.getLocationId(),
                        device.getName(), device.getDeviceCategory(),
                        device.getDeviceSerialNumber(), device.isEnabled(),
                        testResponses);
            }).collect(Collectors.toList());

            return new ZoneWithDevicesResponse(
                    zone.getId(), zone.getLocationId(),
                    zone.getName(), zone.getDetails(),
                    deviceResponses);
        }).collect(Collectors.toList());
    }

    /**
     * Crea una nueva zona en la ubicación indicada.
     * Valida que la ubicación exista antes de crear.
     */
    public ZoneWithDevicesResponse createZone(String locationId, CreateZoneRequest request) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Location not found: " + locationId));

        String id = UUID.randomUUID().toString();
        Zone zone = new Zone();
        zone.setId(id);
        zone.setLocationId(locationId);
        zone.setName(request.getName().trim());
        zone.setDetails(request.getDetails() != null ? request.getDetails().trim() : null);
        zoneRepository.save(zone);
        return new ZoneWithDevicesResponse(
                id, locationId, zone.getName(), zone.getDetails(),
                Collections.emptyList());
    }
}
