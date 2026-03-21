package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.AvatarUploadResponse;
import com.example.tup_final.data.remote.dto.UpdateProfileRequest;
import com.example.tup_final.data.remote.dto.UpdateRoleRequest;
import com.example.tup_final.data.remote.dto.UserProfileResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Retrofit API para operaciones de usuario (perfil).
 *
 * Backend:
 * - GET  /api/users/{id}
 * - PUT  /api/users/{id}
 * - POST /api/users/{id}/avatar
 */
public interface UserApi {

    /**
     * GET /api/users
     * Lista todos los usuarios del sistema (para admin).
     */
    @GET("users")
    Call<List<UserProfileResponse>> getAllUsers();

    /**
     * GET /api/users/{id}
     * Obtiene el perfil de un usuario por su ID.
     */
    @GET("users/{id}")
    Call<UserProfileResponse> getUserProfile(@Path("id") String userId);

    /**
     * PATCH /api/users/{id}/role
     * Actualiza el rol de un usuario (solo INSPECTOR u OPERATOR).
     */
    @PATCH("users/{id}/role")
    Call<UserProfileResponse> updateUserRole(@Path("id") String userId, @Body UpdateRoleRequest request);

    /**
     * PUT /api/users/{id}
     * Actualiza el perfil de un usuario.
     */
    @PUT("users/{id}")
    Call<UserProfileResponse> updateProfile(@Path("id") String userId, @Body UpdateProfileRequest request);

    /**
     * POST /api/users/{id}/avatar
     * Sube una imagen como avatar del usuario (multipart).
     */
    @Multipart
    @POST("users/{id}/avatar")
    Call<AvatarUploadResponse> uploadAvatar(
            @Path("id") String userId,
            @Part MultipartBody.Part file
    );
}
