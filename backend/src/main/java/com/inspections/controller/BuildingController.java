package com.inspections.controller;

import com.inspections.dto.BuildingListResponse;
import com.inspections.dto.BuildingSummaryResponse;
import com.inspections.entity.Building;
import com.inspections.entity.Device;
import com.inspections.entity.Location;
import com.inspections.repository.BuildingRepository;
import com.inspections.repository.DeviceRepository;
import com.inspections.repository.DeviceTypeTestTemplateRepository;
import com.inspections.repository.LocationRepository;
import com.inspections.repository.ZoneRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints de buildings.
 *
 * GET /api/buildings – Lista todos los edificios del catálogo
 * GET /api/buildings/{id}/summary – Resumen de estructura para herencia
 */
@RestController
@RequestMapping("/api/buildings")
@Tag(name = "Buildings", description = "Catálogo de edificios para inspecciones")
public class BuildingController {

    private final BuildingRepository buildingRepository;
    private final LocationRepository locationRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceTypeTestTemplateRepository deviceTypeTestTemplateRepository;

    public BuildingController(BuildingRepository buildingRepository,
                              LocationRepository locationRepository,
                              ZoneRepository zoneRepository,
                              DeviceRepository deviceRepository,
                              DeviceTypeTestTemplateRepository deviceTypeTestTemplateRepository) {
        this.buildingRepository = buildingRepository;
        this.locationRepository = locationRepository;
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.deviceTypeTestTemplateRepository = deviceTypeTestTemplateRepository;
    }

    @GetMapping
    @Operation(summary = "Listar edificios",
               description = "Retorna todos los edificios disponibles para crear inspecciones")
    public ResponseEntity<List<BuildingListResponse>> getBuildings() {
        List<BuildingListResponse> buildings = buildingRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(buildings);
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Resumen de estructura",
               description = "Retorna conteos de locations, zones, devices y tests estimados para herencia")
    public ResponseEntity<BuildingSummaryResponse> getBuildingSummary(
            @Parameter(description = "ID del edificio") @PathVariable String id) {
        if (!buildingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found: " + id);
        }
        List<Location> locations = locationRepository.findByBuildingIdOrderByNameAsc(id);
        int zonesCount = 0;
        int testsEstimate = 0;
        for (Location loc : locations) {
            zonesCount += zoneRepository.findByLocationId(loc.getId()).size();
        }
        List<Device> devices = deviceRepository.findByBuildingId(id);
        for (Device dev : devices) {
            if (dev.isEnabled()) {
                testsEstimate += deviceTypeTestTemplateRepository.findByDeviceTypeIdOrderBySortOrderAsc(dev.getDeviceTypeId()).size();
            }
        }
        BuildingSummaryResponse summary = new BuildingSummaryResponse(
                locations.size(), zonesCount, devices.size(), testsEstimate);
        return ResponseEntity.ok(summary);
    }

    private BuildingListResponse mapToResponse(Building b) {
        return new BuildingListResponse(b.getId(), b.getName(), b.getDetails());
    }
}
