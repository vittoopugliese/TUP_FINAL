package com.inspections.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspections.dto.CreateDeviceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test: POST create device + GET zones returns new device.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DeviceCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDevice_thenGetZones_returnsNewDevice() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setName("Nuevo dispositivo de prueba");
        request.setDeviceCategory("FA_FIELD_DEVICE");
        request.setDescription("Descripción opcional");
        request.setSerialNumber(9999);
        request.setEnabled(true);

        MvcResult createResult = mockMvc.perform(post("/api/locations/loc-001/zones/zone-001/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Nuevo dispositivo de prueba"))
                .andExpect(jsonPath("$.deviceCategory").value("FA_FIELD_DEVICE"))
                .andExpect(jsonPath("$.zoneId").value("zone-001"))
                .andExpect(jsonPath("$.locationId").value("loc-001"))
                .andExpect(jsonPath("$.deviceSerialNumber").value(9999))
                .andExpect(jsonPath("$.enabled").value(true))
                .andReturn();

        String createdId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/locations/loc-001/zones").param("inspectionId", "insp-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].devices[*].id", hasItem(createdId)));
    }

    @Test
    void createDevice_zoneNotFound_returns404() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setName("Device");
        request.setDeviceCategory("FA_FIELD_DEVICE");

        mockMvc.perform(post("/api/locations/loc-001/zones/zone-xxx-nonexistent/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDevice_zoneNotInLocation_returns400() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setName("Device");
        request.setDeviceCategory("FA_FIELD_DEVICE");

        // zone-003 belongs to loc-002, not loc-001
        mockMvc.perform(post("/api/locations/loc-001/zones/zone-003/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDevice_missingName_returns400() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setName("");
        request.setDeviceCategory("FA_FIELD_DEVICE");

        mockMvc.perform(post("/api/locations/loc-001/zones/zone-001/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
