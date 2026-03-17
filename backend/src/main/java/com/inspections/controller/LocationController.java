package com.inspections.controller;

import com.inspections.dto.CreateDeviceRequest;
import com.inspections.dto.CreateLocationRequest;
import com.inspections.dto.CreateZoneRequest;
import com.inspections.dto.DeviceWithTestsResponse;
import com.inspections.dto.LocationListResponse;
import com.inspections.dto.MoveDeviceRequest;
import com.inspections.dto.MoveDeviceResponse;
import com.inspections.dto.ZoneWithDevicesResponse;
import com.inspections.entity.Location;
import com.inspections.repository.LocationRepository;
import com.inspections.service.DeviceService;
import com.inspections.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Endpoints de ubicaciones.
 *
 * GET /api/locations?buildingId=xxx – Lista ubicaciones por edificio
 * GET /api/locations/{locationId}/zones?inspectionId=xxx – Zonas con devices y tests
 */
@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "Operaciones de ubicaciones")
public class LocationController {

    private final LocationRepository locationRepository;
    private final ZoneService zoneService;
    private final DeviceService deviceService;

    public LocationController(LocationRepository locationRepository, ZoneService zoneService,
                              DeviceService deviceService) {
        this.locationRepository = locationRepository;
        this.zoneService = zoneService;
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Listar ubicaciones",
               description = "Retorna ubicaciones filtradas por buildingId. Si buildingId es vacío, retorna todas.")
    public ResponseEntity<List<LocationListResponse>> getLocations(
            @Parameter(description = "ID del edificio para filtrar")
            @RequestParam(required = false) String buildingId) {
        List<Location> locations = (buildingId != null && !buildingId.isBlank())
                ? locationRepository.findByBuildingIdOrderByNameAsc(buildingId)
                : locationRepository.findAll();
        List<LocationListResponse> response = locations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Crear ubicación",
               description = "Crea una nueva ubicación, opcionalmente asociada a un edificio.")
    public ResponseEntity<LocationListResponse> createLocation(
            @Valid @RequestBody CreateLocationRequest request) {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Location location = new Location();
        location.setId(id);
        location.setName(request.getName().trim());
        location.setDetails(request.getDetails() != null && !request.getDetails().isBlank()
                ? request.getDetails().trim() : null);
        location.setBuildingId(request.getBuildingId() != null && !request.getBuildingId().isBlank()
                ? request.getBuildingId().trim() : null);
        location.setCreatedAt(now);
        location.setUpdatedAt(now);

        locationRepository.save(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(location));
    }

    @GetMapping("/{locationId}/zones")
    @Operation(summary = "Obtener zonas con devices y tests",
               description = "Retorna las zonas de una ubicación con sus devices y tests filtrados por inspección.")
    public ResponseEntity<List<ZoneWithDevicesResponse>> getZonesWithDevicesAndTests(
            @PathVariable String locationId,
            @Parameter(description = "ID de la inspección para filtrar tests")
            @RequestParam(required = false) String inspectionId) {
        List<ZoneWithDevicesResponse> response =
                zoneService.getZonesWithDevicesAndTests(locationId, inspectionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{locationId}/zones")
    @Operation(summary = "Crear zona",
               description = "Crea una nueva zona en la ubicación indicada.")
    public ResponseEntity<ZoneWithDevicesResponse> createZone(
            @PathVariable String locationId,
            @Valid @RequestBody CreateZoneRequest request) {
        ZoneWithDevicesResponse created = zoneService.createZone(locationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{locationId}/zones/{zoneId}/devices")
    @Operation(summary = "Crear dispositivo en zona",
               description = "Crea un dispositivo en la zona indicada. La zona debe pertenecer a la ubicación.")
    public ResponseEntity<DeviceWithTestsResponse> createDevice(
            @PathVariable String locationId,
            @PathVariable String zoneId,
            @Valid @RequestBody CreateDeviceRequest request) {
        DeviceWithTestsResponse created = deviceService.createDevice(locationId, zoneId, request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{locationId}/devices/{deviceId}/zone")
    @Operation(summary = "Mover dispositivo a otra zona",
               description = "Mueve el dispositivo a otra zona dentro de la misma ubicación. Los tests se mantienen.")
    public ResponseEntity<MoveDeviceResponse> moveDeviceZone(
            @PathVariable String locationId,
            @PathVariable String deviceId,
            @Valid @RequestBody MoveDeviceRequest request) {
        MoveDeviceResponse result = deviceService.moveDeviceWithinLocation(locationId, deviceId, request);
        return ResponseEntity.ok(result);
    }

    private LocationListResponse mapToResponse(Location loc) {
        return new LocationListResponse(
                loc.getId(),
                loc.getBuildingId(),
                loc.getName(),
                loc.getDetails()
        );
    }
}
