package com.example.tup_final.data.remote;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Retrofit API para endpoints de inspecciones.
 *
 * Rutas previstas:
 * - GET    /api/inspections
 * - GET    /api/inspections/{id}
 * - POST   /api/inspections
 * - PUT    /api/inspections/{id}
 * - DELETE /api/inspections/{id}
 */
public interface InspectionApi {

    @GET("inspections")
    Call<List<JsonObject>> getInspections();

    @GET("inspections/{id}")
    Call<JsonObject> getInspection(@Path("id") String id);

    @POST("inspections")
    Call<JsonObject> createInspection(@Body JsonObject body);

    @PUT("inspections/{id}")
    Call<JsonObject> updateInspection(@Path("id") String id, @Body JsonObject body);

    @DELETE("inspections/{id}")
    Call<Void> deleteInspection(@Path("id") String id);
}

