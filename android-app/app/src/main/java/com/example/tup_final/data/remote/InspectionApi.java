package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.AssignmentRequest;
import com.example.tup_final.data.remote.dto.AssignmentResponse;
import com.example.tup_final.data.remote.dto.CreateInspectionRequest;
import com.example.tup_final.data.remote.dto.CreateInspectionResponse;
import com.example.tup_final.data.remote.dto.InspectionListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit API para endpoints de inspecciones.
 *
 * Rutas:
 * - GET /api/inspections – Lista de inspecciones
 * - POST /api/inspections – Crear inspección building-wide
 * - GET /api/inspections/{id}/assignments – Asignaciones de una inspeccion
 * - POST /api/inspections/{id}/assignments – Agregar asignacion
 * - DELETE /api/inspections/{id}/assignments/{email} – Remover asignacion
 */
public interface InspectionApi {

    @GET("inspections")
    Call<List<InspectionListResponse>> getInspections();

    @POST("inspections")
    Call<CreateInspectionResponse> createInspection(@Body CreateInspectionRequest request);

    @GET("inspections/{id}/assignments")
    Call<List<AssignmentResponse>> getAssignments(@Path("id") String inspectionId);

    @POST("inspections/{id}/assignments")
    Call<AssignmentResponse> addAssignment(@Path("id") String inspectionId, @Body AssignmentRequest body);

    @DELETE("inspections/{id}/assignments/{email}")
    Call<Void> removeAssignment(@Path("id") String inspectionId, @Path("email") String email);
}
