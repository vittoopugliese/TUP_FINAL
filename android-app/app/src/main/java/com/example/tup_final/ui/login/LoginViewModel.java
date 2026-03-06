package com.example.tup_final.ui.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.remote.dto.LoginResponse;
import com.example.tup_final.data.repository.AuthRepository;
import com.example.tup_final.util.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de Login.
 * Valida campos localmente antes de delegar al repositorio.
 * Expone loginResult como LiveData<Resource<LoginResponse>>.
 */
@HiltViewModel
public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MediatorLiveData<Resource<LoginResponse>> loginResult = new MediatorLiveData<>();

    @Inject
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Resultado observable del intento de login.
     */
    public LiveData<Resource<LoginResponse>> getLoginResult() {
        return loginResult;
    }

    /**
     * Ejecuta el login. Valida campos antes de llamar al backend.
     *
     * @param email    email del usuario
     * @param password contraseña del usuario
     */
    public void login(String email, String password) {
        // Validación local
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            loginResult.setValue(Resource.error("Completá todos los campos."));
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            loginResult.setValue(Resource.error("Formato de email inválido."));
            return;
        }

        // Indicar estado de carga
        loginResult.setValue(Resource.loading());

        // Delegar al repositorio y conectar su LiveData
        LiveData<Resource<LoginResponse>> source = authRepository.login(email.trim(), password);
        loginResult.addSource(source, resource -> {
            loginResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                loginResult.removeSource(source);
            }
        });
    }
}
