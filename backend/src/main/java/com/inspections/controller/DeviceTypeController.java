package com.inspections.controller;

import com.inspections.dto.DeviceTypeResponse;
import com.inspections.entity.DeviceType;
import com.inspections.repository.DeviceTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints read-only para el catálogo global de tipos de dispositivo.
 */
@RestController
@RequestMapping("/api/device-types")
@Tag(name = "Device Types", description = "Catálogo de tipos de dispositivos (read-only)")
public class DeviceTypeController {

    private final DeviceTypeRepository deviceTypeRepository;

    public DeviceTypeController(DeviceTypeRepository deviceTypeRepository) {
        this.deviceTypeRepository = deviceTypeRepository;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de dispositivo",
               description = "Retorna el catálogo global. Por defecto solo tipos habilitados.")
    public ResponseEntity<List<DeviceTypeResponse>> getDeviceTypes(
            @Parameter(description = "Incluir tipos deshabilitados")
            @RequestParam(required = false, defaultValue = "false") boolean includeDisabled) {
        List<DeviceType> types = includeDisabled
                ? deviceTypeRepository.findAllByOrderBySortOrderAsc()
                : deviceTypeRepository.findByEnabledTrueOrderBySortOrderAsc();
        List<DeviceTypeResponse> response = types.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo por ID")
    public ResponseEntity<DeviceTypeResponse> getDeviceType(@PathVariable String id) {
        return deviceTypeRepository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private DeviceTypeResponse mapToResponse(DeviceType t) {
        return new DeviceTypeResponse(
                t.getId(),
                t.getCode(),
                t.getName(),
                t.getCategory(),
                t.isEnabled()
        );
    }
}
