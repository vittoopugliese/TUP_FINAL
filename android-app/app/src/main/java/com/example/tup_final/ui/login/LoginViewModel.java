package com.example.tup_final.ui.login;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

/**
 * ViewModel for login screen validation.
 * Validates email format (RFC 5322 via Patterns.EMAIL_ADDRESS) and password minimum length.
 */
@HiltViewModel
public class LoginViewModel extends ViewModel {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();

    @Inject
    public LoginViewModel() {
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    /**
     * Validates email and password. Sets error LiveData values when validation fails.
     *
     * @return true if both email and password are valid
     */
    public boolean validate(String email, String password) {
        boolean valid = true;

        if (email == null || email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
}
