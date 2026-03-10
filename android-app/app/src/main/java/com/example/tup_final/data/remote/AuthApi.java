package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.LoginRequest;
import com.example.tup_final.data.remote.dto.LoginResponse;
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
     * Body: { "email": "...", "password": "..." }
     * Response: { token, type, email, role, userId, fullName }
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /**
     * POST /api/auth/logout
     * Backend es stateless: el cliente descarta el token.
     */
    @POST("auth/logout")
    Call<Void> logout();

    /**
     * POST /api/auth/refresh
     * Header: Authorization: Bearer <token>
     */
    @POST("auth/refresh")
    Call<JsonObject> refresh(@Header("Authorization") String authorizationHeader);

    /**
     * POST /api/auth/forgot-password
     * Body esperado:
     * { "email": "..." }
     */
    @POST("auth/forgot-password")
    Call<JsonObject> forgotPassword(@Body JsonObject request);
}
