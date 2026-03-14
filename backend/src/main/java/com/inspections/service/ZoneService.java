package com.inspections.service;

import com.inspections.dto.DeviceWithTestsResponse;
import com.inspections.dto.TestResponse;
import com.inspections.dto.ZoneWithDevicesResponse;
import com.inspections.entity.Device;
import com.inspections.entity.InspectionTest;
import com.inspections.entity.Zone;
import com.inspections.repository.DeviceRepository;
import com.inspections.repository.TestRepository;
import com.inspections.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para construir la jerarquía Zona -> Devices -> Tests.
 */
@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final TestRepository testRepository;

    public ZoneService(ZoneRepository zoneRepository,
                       DeviceRepository deviceRepository,
                       TestRepository testRepository) {
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.testRepository = testRepository;
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
}
