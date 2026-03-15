package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.BuildingListResponse;
import com.example.tup_final.data.remote.dto.BuildingSummaryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit API para el catálogo de buildings.
 * GET /api/buildings – Lista edificios
 * GET /api/buildings/{id}/summary – Resumen de estructura
 */
public interface BuildingApi {

    @GET("buildings")
    Call<List<BuildingListResponse>> getBuildings();

    @GET("buildings/{id}/summary")
    Call<BuildingSummaryResponse> getBuildingSummary(@Path("id") String buildingId);
}
