package com.inspections.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspections.dto.MoveDeviceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: PATCH move device between zones in same location.
 * Tests preserve device and its tests; only zone grouping changes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DeviceMoveIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void moveDevice_thenGetZones_reflectsNewZone() throws Exception {
        // dev-001 is in zone-002, loc-001. Move to zone-001 (same location).
        MoveDeviceRequest request = new MoveDeviceRequest();
        request.setTargetZoneId("zone-001");

        mockMvc.perform(patch("/api/locations/loc-001/devices/dev-001/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("dev-001"))
                .andExpect(jsonPath("$.oldZoneId").value("zone-002"))
                .andExpect(jsonPath("$.newZoneId").value("zone-001"))
                .andExpect(jsonPath("$.locationId").value("loc-001"))
                .andExpect(jsonPath("$.updatedAt").exists());

        // Verify device appears in zone-001 and not in zone-002
        mockMvc.perform(get("/api/locations/loc-001/zones").param("inspectionId", "insp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='zone-001')].devices[*].id", hasItem("dev-001")))
                .andExpect(jsonPath("$[?(@.id=='zone-002')].devices[*].id", not(hasItem("dev-001"))));

        // Move back to zone-002 for other tests
        MoveDeviceRequest revert = new MoveDeviceRequest();
        revert.setTargetZoneId("zone-002");
        mockMvc.perform(patch("/api/locations/loc-001/devices/dev-001/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(revert)))
                .andExpect(status().isOk());
    }

    @Test
    void moveDevice_sameZone_returns400() throws Exception {
        MoveDeviceRequest request = new MoveDeviceRequest();
        request.setTargetZoneId("zone-002"); // dev-001 is already in zone-002

        mockMvc.perform(patch("/api/locations/loc-001/devices/dev-001/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveDevice_deviceNotFound_returns404() throws Exception {
        MoveDeviceRequest request = new MoveDeviceRequest();
        request.setTargetZoneId("zone-001");

        mockMvc.perform(patch("/api/locations/loc-001/devices/dev-xxx-nonexistent/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveDevice_targetZoneNotInLocation_returns400() throws Exception {
        MoveDeviceRequest request = new MoveDeviceRequest();
        request.setTargetZoneId("zone-005"); // zone-005 belongs to loc-004, not loc-001

        mockMvc.perform(patch("/api/locations/loc-001/devices/dev-001/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveDevice_deviceNotInLocation_returns400() throws Exception {
        MoveDeviceRequest request = new MoveDeviceRequest();
        request.setTargetZoneId("zone-001");

        // dev-001 is in loc-001; loc-002 is different location
        mockMvc.perform(patch("/api/locations/loc-002/devices/dev-001/zone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
