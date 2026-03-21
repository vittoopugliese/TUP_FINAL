package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.DeficiencyTypeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Retrofit API para el catálogo de tipos de deficiencia.
 * GET /api/deficiency-types — retorna todos los tipos habilitados.
 */
public interface DeficiencyTypeApi {

    @GET("deficiency-types")
    Call<List<DeficiencyTypeResponse>> getDeficiencyTypes();
}
