package com.example.tup_final.data.remote;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit API para autenticación contra el backend Spring Boot.
 *
 * Backend:
 * - POST /api/auth/login
 * - POST /api/auth/logout
 * - POST /api/auth/refresh
 */
public interface AuthApi {

    /**
     * POST /api/auth/login
     * Body esperado:
     * { "email": "...", "password": "..." }
     */
    @POST("api/auth/login")
    Call<JsonObject> login(@Body JsonObject request);

    /**
     * POST /api/auth/logout
     * Backend es stateless: el cliente descarta el token.
     */
    @POST("api/auth/logout")
    Call<Void> logout();

    /**
     * POST /api/auth/refresh
     * Header: Authorization: Bearer <token>
     */
    @POST("api/auth/refresh")
    Call<JsonObject> refresh(@Header("Authorization") String authorizationHeader);
}

