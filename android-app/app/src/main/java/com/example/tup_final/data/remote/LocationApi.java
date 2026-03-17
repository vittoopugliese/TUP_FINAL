package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.CreateLocationRequest;
import com.example.tup_final.data.remote.dto.LocationListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit API para endpoints de ubicaciones.
 *
 * Rutas:
 * - GET /api/locations?buildingId=xxx – Lista ubicaciones por edificio
 * - POST /api/locations – Crear ubicación
 */
public interface LocationApi {

    @GET("locations")
    Call<List<LocationListResponse>> getLocations(@Query("buildingId") String buildingId);

    @POST("locations")
    Call<LocationListResponse> createLocation(@Body CreateLocationRequest request);
}
