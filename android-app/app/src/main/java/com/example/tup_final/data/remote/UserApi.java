package com.example.tup_final.data.remote;

import com.example.tup_final.data.remote.dto.AvatarUploadResponse;
import com.example.tup_final.data.remote.dto.UpdateProfileRequest;
import com.example.tup_final.data.remote.dto.UserProfileResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
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
     * GET /api/users/{id}
     * Obtiene el perfil de un usuario por su ID.
     */
    @GET("users/{id}")
    Call<UserProfileResponse> getUserProfile(@Path("id") String userId);

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
