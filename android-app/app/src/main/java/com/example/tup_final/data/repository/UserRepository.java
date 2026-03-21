package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.local.UserDao;
import com.example.tup_final.data.remote.UserApi;
import com.example.tup_final.data.remote.dto.AvatarUploadResponse;
import com.example.tup_final.data.remote.dto.UpdateProfileRequest;
import com.example.tup_final.data.remote.dto.UpdateRoleRequest;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.example.tup_final.util.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Repository para el perfil de usuario.
 * Intenta obtener datos online (UserApi), guarda en Room,
 * y usa Room como fallback offline.
 */
@Singleton
public class UserRepository {

    private final UserApi userApi;
    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public UserRepository(UserApi userApi, UserDao userDao) {
        this.userApi = userApi;
        this.userDao = userDao;
    }

    /**
     * Obtiene el perfil del usuario.
     * Online: llama al backend y guarda en Room.
     * Offline: lee de Room como fallback.
     *
     * @param userId ID del usuario
     * @return LiveData con el estado del recurso
     */
    public LiveData<Resource<UserEntity>> getUserProfile(String userId) {
        MutableLiveData<Resource<UserEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                // Intentar online
                UserProfileResponse response = fetchOnline(userId);
                UserEntity entity = mapToEntity(response);
                userDao.insert(entity);
                mainHandler.post(() -> result.setValue(Resource.success(entity)));
            } catch (IOException e) {
                // Fallback offline
                UserEntity cached = userDao.getById(userId);
                if (cached != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(cached)));
                } else {
                    mainHandler.post(() -> result.setValue(
                            Resource.error("No se pudo cargar el perfil. Verificá tu conexión.")));
                }
            }
        });

        return result;
    }

    /**
     * Actualiza el perfil del usuario.
     * 1) Guarda en Room (optimistic update)
     * 2) Intenta enviar al backend
     * 3) Si éxito: actualiza Room con respuesta
     * 4) Si falla (offline): mantiene datos en Room, emite success con mensaje de sync pendiente
     */
    public LiveData<Resource<UserEntity>> updateProfile(String userId, UpdateProfileRequest request) {
        MutableLiveData<Resource<UserEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            UserEntity current = userDao.getById(userId);
            if (current == null) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("No se encontró el perfil. Recargá la pantalla.")));
                return;
            }

            // 1) Optimistic update en Room
            UserEntity updated = new UserEntity(
                    current.id,
                    current.email,
                    request.getFirstName() != null ? request.getFirstName() : current.firstName,
                    request.getLastName() != null ? request.getLastName() : current.lastName,
                    request.getAvatarImage() != null ? request.getAvatarImage() : current.avatarImage,
                    request.getPhoneNumber() != null ? request.getPhoneNumber() : current.phoneNumber,
                    current.role,
                    current.lastLoginAt,
                    current.createdAt
            );
            userDao.update(updated);

            // 2) Intentar enviar al backend
            try {
                Response<UserProfileResponse> response = userApi.updateProfile(userId, request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    UserEntity fromBackend = mapToEntity(response.body());
                    userDao.update(fromBackend);
                    mainHandler.post(() -> result.setValue(Resource.success(fromBackend)));
                } else {
                    mainHandler.post(() -> result.setValue(
                            Resource.success(updated))); // Local OK, sync pendiente
                }
            } catch (IOException e) {
                // Offline o error de red: datos ya guardados en Room
                mainHandler.post(() -> result.setValue(Resource.success(updated)));
            }
        });

        return result;
    }

    /**
     * Sube el avatar del usuario al backend.
     * Envía la imagen como multipart, actualiza Room con la URL retornada.
     *
     * @param userId    ID del usuario
     * @param imageFile Archivo de imagen comprimido
     * @return LiveData con el estado del recurso
     */
    public LiveData<Resource<UserEntity>> uploadAvatar(String userId, File imageFile) {
        MutableLiveData<Resource<UserEntity>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                // Crear MultipartBody.Part
                RequestBody requestBody = RequestBody.create(
                        imageFile, MediaType.parse("image/jpeg"));
                MultipartBody.Part part = MultipartBody.Part.createFormData(
                        "file", imageFile.getName(), requestBody);

                // Llamar al backend
                Response<AvatarUploadResponse> response =
                        userApi.uploadAvatar(userId, part).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String avatarUrl = response.body().getAvatarUrl();

                    // Actualizar Room
                    UserEntity user = userDao.getById(userId);
                    if (user != null) {
                        user.avatarImage = avatarUrl;
                        userDao.update(user);
                        mainHandler.post(() -> result.setValue(Resource.success(user)));
                    } else {
                        mainHandler.post(() -> result.setValue(
                                Resource.error("No se encontró el perfil local.")));
                    }
                } else {
                    mainHandler.post(() -> result.setValue(
                            Resource.error("Error al subir la imagen. Intentá de nuevo.")));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("Error de conexión. Verificá tu internet.")));
            }
        });

        return result;
    }

    /**
     * Lista todos los usuarios del sistema (para admin).
     */
    public LiveData<Resource<List<UserProfileResponse>>> getAllUsers() {
        MutableLiveData<Resource<List<UserProfileResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<UserProfileResponse>> response = userApi.getAllUsers().execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String msg = parseErrorMessage(response.errorBody());
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("Error de conexión. Verificá tu internet.")));
            }
        });

        return result;
    }

    /**
     * Actualiza el rol de un usuario (solo INSPECTOR u OPERATOR).
     */
    public LiveData<Resource<UserProfileResponse>> updateUserRole(String userId, String newRole) {
        MutableLiveData<Resource<UserProfileResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<UserProfileResponse> response = userApi.updateUserRole(userId, new UpdateRoleRequest(newRole)).execute();
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse updated = response.body();
                    // Actualizar Room si el usuario está cacheado
                    UserEntity cached = userDao.getById(userId);
                    if (cached != null) {
                        cached.role = updated.getRole();
                        userDao.update(cached);
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(updated)));
                } else {
                    String msg = parseErrorMessage(response.errorBody());
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (IOException e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error("Error de conexión. Verificá tu internet.")));
            }
        });

        return result;
    }

    private String parseErrorMessage(ResponseBody errorBody) {
        if (errorBody == null) return "Error desconocido";
        try {
            return errorBody.string();
        } catch (IOException e) {
            return "Error desconocido";
        }
    }

    private UserProfileResponse fetchOnline(String userId) throws IOException {
        Response<UserProfileResponse> response = userApi.getUserProfile(userId).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Error al obtener perfil: " + response.code());
        }
        return response.body();
    }

    private UserEntity mapToEntity(UserProfileResponse dto) {
        return new UserEntity(
                dto.getId(),
                dto.getEmail(),
                dto.getFirstName(),
                dto.getLastName(),
                dto.getAvatarImage(),
                dto.getPhoneNumber(),
                dto.getRole(),
                null, // lastLoginAt
                null  // createdAt
        );
    }
}
