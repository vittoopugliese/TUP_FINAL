package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.InspectionListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit API para endpoints de inspecciones.
 *
 * Rutas:
 * - GET /api/inspections – Lista de inspecciones
 */
public interface InspectionApi {

    @GET("inspections")
    Call<List<InspectionListResponse>> getInspections();
}
