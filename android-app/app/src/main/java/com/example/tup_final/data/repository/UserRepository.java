package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.local.UserDao;
import com.example.tup_final.data.remote.UserApi;
import com.example.tup_final.data.remote.dto.UserProfileResponse;
import com.example.tup_final.util.Resource;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    private UserProfileResponse fetchOnline(String userId) throws IOException {
        retrofit2.Response<UserProfileResponse> response = userApi.getUserProfile(userId).execute();
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
