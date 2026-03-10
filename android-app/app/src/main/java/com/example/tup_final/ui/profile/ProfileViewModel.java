package com.example.tup_final.ui.profile;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.repository.UserRepository;
import com.example.tup_final.util.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de perfil.
 * Obtiene el userId de SharedPreferences y carga los datos del perfil.
 */
@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final SharedPreferences prefs;
    private MutableLiveData<Resource<UserEntity>> profile;

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
}
