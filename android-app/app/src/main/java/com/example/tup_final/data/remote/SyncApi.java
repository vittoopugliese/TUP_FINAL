package com.example.tup_final.data.remote;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit API para endpoints de sincronización de datos.
 *
 * Rutas previstas:
 * - POST /api/sync/push
 * - POST /api/sync/pull
 */
public interface SyncApi {

    /**
     * Envía cambios locales pendientes al backend.
     * Ejemplo de body: { "changes": [...], "lastSyncAt": "..." }
     */
    @POST("api/sync/push")
    Call<JsonObject> pushChanges(@Body JsonObject payload);

    /**
     * Solicita cambios desde la última sincronización.
     * Ejemplo de body: { "lastSyncAt": "..." }
     */
    @POST("api/sync/pull")
    Call<JsonObject> pullChanges(@Body JsonObject payload);
}

