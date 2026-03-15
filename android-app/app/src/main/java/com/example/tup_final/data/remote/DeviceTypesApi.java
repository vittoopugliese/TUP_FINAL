package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.DeviceTypeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API para el catálogo de tipos de dispositivo.
 * GET /api/device-types
 */
public interface DeviceTypesApi {

    @GET("device-types")
    Call<List<DeviceTypeResponse>> getDeviceTypes(
            @Query("includeDisabled") boolean includeDisabled
    );

    @GET("device-types/{id}")
    Call<DeviceTypeResponse> getDeviceType(@Path("id") String id);
}
