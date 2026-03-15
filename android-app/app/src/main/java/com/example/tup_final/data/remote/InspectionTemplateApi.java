package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.InspectionTemplateListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit API para el catálogo de plantillas de inspección.
 * GET /api/inspection-templates – Lista plantillas
 */
public interface InspectionTemplateApi {

    @GET("inspection-templates")
    Call<List<InspectionTemplateListResponse>> getTemplates();
}
