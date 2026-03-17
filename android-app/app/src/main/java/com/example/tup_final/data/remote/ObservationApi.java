package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.CreateObservationRequest;
import com.example.tup_final.data.remote.dto.ObservationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit API para observaciones y deficiencias.
 * Base URL: /api/
 *
 * POST /api/steps/{stepId}/observations  – Crear observación
 * GET  /api/steps/{stepId}/observations  – Listar por step
 */
public interface ObservationApi {

    @POST("steps/{stepId}/observations")
    Call<ObservationResponse> createObservation(
            @Path("stepId") String stepId,
            @Body CreateObservationRequest request);

    @GET("steps/{stepId}/observations")
    Call<List<ObservationResponse>> getObservationsByStep(
            @Path("stepId") String stepId);
}
