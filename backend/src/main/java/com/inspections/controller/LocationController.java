package com.inspections.controller;

import com.inspections.dto.LocationListResponse;
import com.inspections.dto.ZoneWithDevicesResponse;
import com.inspections.entity.Location;
import com.inspections.repository.LocationRepository;
import com.inspections.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    public LocationController(LocationRepository locationRepository, ZoneService zoneService) {
        this.locationRepository = locationRepository;
        this.zoneService = zoneService;
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

    private LocationListResponse mapToResponse(Location loc) {
        return new LocationListResponse(
                loc.getId(),
                loc.getBuildingId(),
                loc.getName(),
                loc.getDetails()
        );
    }
}
