package com.example.tup_final.ui.profile;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.remote.dto.UpdateProfileRequest;
import com.example.tup_final.data.repository.UserRepository;
import com.example.tup_final.util.Resource;

import java.io.File;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de perfil.
 * Obtiene el userId de SharedPreferences y carga los datos del perfil.
 * Soporta edición con validación, actualización y subida de avatar.
 */
@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s\\-()]*$");

    private final UserRepository userRepository;
    private final SharedPreferences prefs;
    private MutableLiveData<Resource<UserEntity>> profile;
    private final MediatorLiveData<Resource<UserEntity>> updateResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<UserEntity>> uploadResult = new MediatorLiveData<>();

    @Inject
    public ProfileViewModel(UserRepository userRepository, SharedPreferences prefs) {
        this.userRepository = userRepository;
        this.prefs = prefs;
    }

    /**
     * Retorna LiveData con el perfil del usuario.
     * Carga automáticamente la primera vez.
     */
    public LiveData<Resource<UserEntity>> getProfile() {
        if (profile == null) {
            loadProfile();
        }
        return profile;
    }

    /**
     * Resultado del guardado de perfil.
     */
    public LiveData<Resource<UserEntity>> getUpdateResult() {
        return updateResult;
    }

    /**
     * Resultado de la subida de avatar.
     */
    public LiveData<Resource<UserEntity>> getUploadResult() {
        return uploadResult;
    }

    /**
     * Carga (o recarga) el perfil del usuario desde el repository.
     */
    public void loadProfile() {
        String userId = prefs.getString("cached_user_id", null);
        if (userId == null || userId.isEmpty()) {
            profile = new MutableLiveData<>(Resource.error("No se encontró el ID del usuario. Iniciá sesión nuevamente."));
            return;
        }
        profile = (MutableLiveData<Resource<UserEntity>>) userRepository.getUserProfile(userId);
    }

    /**
     * Valida los campos de edición del perfil.
     *
     * @return Mensaje de error o null si es válido
     */
    public String validateProfileEdit(String firstName, String lastName, String phoneNumber) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return "error_name_required";
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            return "error_lastname_required";
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(phoneNumber.trim()).matches()) {
                return "error_phone_invalid";
            }
        }
        return null;
    }

    /**
     * Actualiza el perfil del usuario.
     * Valida, guarda en Room y sincroniza con backend si hay conexión.
     */
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        String errorKey = validateProfileEdit(firstName, lastName, phoneNumber);
        if (errorKey != null) {
            updateResult.setValue(Resource.error(errorKey));
            return;
        }

        String userId = prefs.getString("cached_user_id", null);
        if (userId == null || userId.isEmpty()) {
            updateResult.setValue(Resource.error("No se encontró el ID del usuario."));
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest(
                firstName != null ? firstName.trim() : null,
                lastName != null ? lastName.trim() : null,
                phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber.trim() : null,
                null  // avatarImage - se maneja por separado con uploadAvatar
        );

        LiveData<Resource<UserEntity>> source = userRepository.updateProfile(userId, request);
        updateResult.addSource(source, resource -> {
            updateResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                updateResult.removeSource(source);
            }
        });
    }

    /**
     * Sube una imagen como avatar del usuario.
     *
     * @param imageFile Archivo de imagen comprimido (JPEG)
     */
    public void uploadAvatar(File imageFile) {
        String userId = prefs.getString("cached_user_id", null);
        if (userId == null || userId.isEmpty()) {
            uploadResult.setValue(Resource.error("No se encontró el ID del usuario."));
            return;
        }

        LiveData<Resource<UserEntity>> source = userRepository.uploadAvatar(userId, imageFile);
        uploadResult.addSource(source, resource -> {
            uploadResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                uploadResult.removeSource(source);
            }
        });
    }
}
