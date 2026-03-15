package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.CreateDeviceRequest;
import com.example.tup_final.data.remote.dto.DeviceWithTestsResponse;
import com.example.tup_final.data.remote.dto.ZoneWithDevicesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API para zonas, devices y tests.
 *
 * Rutas:
 * - GET /locations/{locationId}/zones?inspectionId=xxx – Zonas con devices y tests
 * - POST /locations/{locationId}/zones/{zoneId}/devices – Crear dispositivo en zona
 */
public interface ZonesApi {

    /**
     * Obtiene las zonas de una ubicación con sus devices y tests para una inspección.
     * Backend esperado: GET /api/locations/{locationId}/zones?inspectionId=xxx
     */
    @GET("locations/{locationId}/zones")
    Call<List<ZoneWithDevicesResponse>> getZonesWithDevicesAndTests(
            @Path("locationId") String locationId,
            @Query("inspectionId") String inspectionId
    );

    /**
     * Crea un dispositivo en la zona indicada.
     * Backend: POST /api/locations/{locationId}/zones/{zoneId}/devices
     */
    @POST("locations/{locationId}/zones/{zoneId}/devices")
    Call<DeviceWithTestsResponse> createDevice(
            @Path("locationId") String locationId,
            @Path("zoneId") String zoneId,
            @Body CreateDeviceRequest request
    );
}
