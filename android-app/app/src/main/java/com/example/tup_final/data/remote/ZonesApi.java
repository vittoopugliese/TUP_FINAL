package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.ZoneWithDevicesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API para zonas, devices y tests.
 *
 * Rutas:
 * - GET /locations/{locationId}/zones?inspectionId=xxx – Zonas con devices y tests
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
}
