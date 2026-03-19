package com.example.tup_final.ui.register;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.remote.dto.RegisterResponse;
import com.example.tup_final.data.repository.AuthRepository;
import com.example.tup_final.util.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de Registro.
 * Valida campos localmente antes de delegar al repositorio.
 */
@HiltViewModel
public class RegisterViewModel extends ViewModel {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository authRepository;

    private final MediatorLiveData<Resource<RegisterResponse>> registerResult = new MediatorLiveData<>();

    @Inject
    public RegisterViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<Resource<RegisterResponse>> getRegisterResult() {
        return registerResult;
    }

    /**
     * Valida los campos del formulario de registro.
     *
     * @return mensaje de error o null si todo es válido
     */
    public String validate(String email, String fullName, String password, String confirmPassword) {
        if (email == null || email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "error_email_invalid";
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            return "error_name_required";
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return "error_password_short";
        }
        if (!password.equals(confirmPassword)) {
            return "error_password_mismatch";
        }
        return null;
    }

    /**
     * Ejecuta el registro. Valida campos antes de llamar al backend.
     * El rol se asigna automáticamente como INSPECTOR.
     */
    public void register(String email, String fullName, String password, String confirmPassword) {
        String validationError = validate(email, fullName, password, confirmPassword);
        if (validationError != null) {
            registerResult.setValue(Resource.error(validationError));
            return;
        }

        registerResult.setValue(Resource.loading());

        LiveData<Resource<RegisterResponse>> source = authRepository.register(
                email.trim(),
                fullName.trim(),
                password
        );
        registerResult.addSource(source, resource -> {
            registerResult.setValue(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                registerResult.removeSource(source);
            }
        });
    }
}
