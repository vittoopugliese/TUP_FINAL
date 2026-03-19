package com.example.tup_final.ui.forgotpassword;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tup_final.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel para la pantalla de recuperación de contraseña.
 * Valida el email y ejecuta el flujo online/offline.
 */
@HiltViewModel
public class ForgotPasswordViewModel extends ViewModel {

    /** Posibles resultados de la solicitud de recuperación */
    public enum ResetResult {
        SUCCESS_ONLINE,
        SUCCESS_OFFLINE,
        ERROR_NOT_FOUND,
        ERROR_NETWORK
    }

    private final AuthRepository authRepository;

    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<ResetResult> resetResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    @Inject
    public ForgotPasswordViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<ResetResult> getResetResult() {
        return resetResult;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * Valida el formato del email.
     *
     * @return true si el email es válido
     */
    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()
                || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("error_email_invalid");
            return false;
        }
        emailError.setValue(null);
        return true;
    }

    /**
     * Envía la solicitud de recuperación de contraseña.
     * Primero intenta online; si falla por red, hace fallback a offline.
     */
    public void submitEmail(String email) {
        if (!validateEmail(email)) {
            return;
        }

        loading.setValue(true);
        authRepository.requestPasswordReset(email.trim(), result -> {
            loading.postValue(false);
            resetResult.postValue(result);
        });
    }
}
