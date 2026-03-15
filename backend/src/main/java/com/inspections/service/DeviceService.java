package com.inspections.service;

import com.inspections.dto.CreateDeviceRequest;
import com.inspections.dto.DeviceWithTestsResponse;
import com.inspections.entity.Device;
import com.inspections.entity.Location;
import com.inspections.entity.Zone;
import com.inspections.repository.DeviceRepository;
import com.inspections.repository.LocationRepository;
import com.inspections.repository.ZoneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * Servicio para operaciones CRUD de dispositivos.
 */
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;

    public DeviceService(DeviceRepository deviceRepository,
                         ZoneRepository zoneRepository,
                         LocationRepository locationRepository) {
        this.deviceRepository = deviceRepository;
        this.zoneRepository = zoneRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Crea un dispositivo en la zona indicada.
     * Valida que la zona exista y pertenezca a la ubicación.
     */
    public DeviceWithTestsResponse createDevice(String locationId, String zoneId, CreateDeviceRequest request) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zone not found: " + zoneId));

        if (!zone.getLocationId().equals(locationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Zone " + zoneId + " does not belong to location " + locationId);
        }

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found: " + locationId));

        Device device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.setZoneId(zoneId);
        device.setLocationId(locationId);
        device.setBuildingId(location.getBuildingId());
        device.setName(request.getName().trim());
        device.setDeviceCategory(request.getDeviceCategory().trim());
        device.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        device.setDeviceSerialNumber(request.getSerialNumber());
        device.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        device.setCreatedAt(Instant.now());
        device.setUpdatedAt(Instant.now());

        device = deviceRepository.save(device);

        return new DeviceWithTestsResponse(
                device.getId(),
                device.getZoneId(),
                device.getLocationId(),
                device.getName(),
                device.getDeviceCategory(),
                device.getDeviceSerialNumber(),
                device.isEnabled(),
                Collections.emptyList()
        );
    }
}
