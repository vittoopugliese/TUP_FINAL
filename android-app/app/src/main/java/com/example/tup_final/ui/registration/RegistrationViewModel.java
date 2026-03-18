package com.example.tup_final.ui.registration;

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
 * ViewModel para la pantalla de Registro.
 * Valida nombre, email y contraseña antes de delegar al repositorio.
 */
@HiltViewModel
public class RegistrationViewModel extends ViewModel {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository authRepository;

    private final MediatorLiveData<Resource<LoginResponse>> registerResult = new MediatorLiveData<>();
    private final MutableLiveData<String> nameError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

    @Inject
    public RegistrationViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Resource<LoginResponse>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<String> getNameError() {
        return nameError;
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    /**
     * Valida nombre, email y contraseña.
     */
    public boolean validate(String name, String email, String password) {
        boolean valid = true;

        if (name == null || name.trim().isEmpty()) {
            nameError.setValue("error_name_required");
            valid = false;
        } else {
            nameError.setValue(null);
        }

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

    public void register(String name, String email, String password) {
        if (!validate(name, email, password)) {
            return;
        }

        registerResult.setValue(Resource.loading());

        LiveData<Resource<LoginResponse>> source = authRepository.register(
                name != null ? name.trim() : "",
                email != null ? email.trim() : "",
                password != null ? password : ""
        );
        registerResult.addSource(source, resource -> {
            registerResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                registerResult.removeSource(source);
            }
        });
    }
}
