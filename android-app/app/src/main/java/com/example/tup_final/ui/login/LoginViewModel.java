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
 * Valida campos localmente (formato de email, longitud de contraseña) antes de delegar al repositorio.
 * Expone loginResult como LiveData<Resource<LoginResponse>> y errores de validación local.
 */
@HiltViewModel
public class LoginViewModel extends ViewModel {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository authRepository;

    private final MediatorLiveData<Resource<LoginResponse>> loginResult = new MediatorLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

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
     * LiveData para errores de formato de email.
     */
    public LiveData<String> getEmailError() {
        return emailError;
    }

    /**
     * LiveData para errores de longitud de contraseña.
     */
    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    /**
     * Valida email y contraseña. Establece los valores de LiveData de error cuando la validación falla.
     *
     * @param email    email del usuario
     * @param password contraseña del usuario
     * @return true si tanto el email como la contraseña son válidos
     */
    public boolean validate(String email, String password) {
        boolean valid = true;

        if (email == null || email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            emailError.setValue("error_email_invalid");
            valid = false;
        } else {
            emailError.setValue(null);
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            passwordError.setValue("error_password_short");
            valid = false;
        } else {
            passwordError.setValue(null);
        }

        return valid;
    }

    /**
     * Ejecuta el login. Valida campos antes de llamar al backend.
     *
     * @param email    email del usuario
     * @param password contraseña del usuario
     */
    public void login(String email, String password) {
        // Validación local
        if (!validate(email, password)) {
            // Los errores se establecen en emailError/passwordError LiveData por validate()
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