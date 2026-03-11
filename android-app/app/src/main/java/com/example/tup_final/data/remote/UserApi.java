package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.UserProfileResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit API para operaciones de usuario (perfil).
 *
 * Backend:
 * - GET /api/users/{id}
 */
public interface UserApi {

    /**
     * GET /api/users/{id}
     * Obtiene el perfil de un usuario por su ID.
     */
    @GET("users/{id}")
    Call<UserProfileResponse> getUserProfile(@Path("id") String userId);
}
