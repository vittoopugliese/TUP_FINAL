package com.inspections.controller;

import com.inspections.dto.LocationListResponse;
import com.inspections.entity.Location;
import com.inspections.repository.LocationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints de ubicaciones.
 *
 * GET /api/locations?buildingId=xxx – Lista ubicaciones por edificio
 */
@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations", description = "Operaciones de ubicaciones")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
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

    private LocationListResponse mapToResponse(Location loc) {
        return new LocationListResponse(
                loc.getId(),
                loc.getBuildingId(),
                loc.getName(),
                loc.getDetails()
        );
    }
}
