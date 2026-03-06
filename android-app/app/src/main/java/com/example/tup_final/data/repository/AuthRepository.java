package com.example.tup_final.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.remote.AuthApi;
import com.example.tup_final.data.remote.dto.LoginRequest;
import com.example.tup_final.data.remote.dto.LoginResponse;
import com.example.tup_final.util.Resource;

import java.net.SocketTimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio de autenticación.
 * Encapsula la llamada a AuthApi y traduce errores HTTP
 * (401, 403, timeout, formato inválido) a mensajes legibles.
 */
@Singleton
public class AuthRepository {

    private final AuthApi authApi;

    @Inject
    public AuthRepository(AuthApi authApi) {
        this.authApi = authApi;
    }

    /**
     * Ejecuta login contra el backend.
     * Retorna LiveData con Resource que encapsula LOADING → SUCCESS/ERROR.
     */
    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                    @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    String errorMsg = mapHttpError(response.code());
                    result.postValue(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call,
                    @NonNull Throwable t) {
                String errorMsg = mapNetworkError(t);
                result.postValue(Resource.error(errorMsg));
            }
        });

        return result;
    }

    /**
     * Traduce códigos HTTP a mensajes de error para el usuario.
     */
    private String mapHttpError(int code) {
        switch (code) {
            case 401:
                return "Credenciales inválidas. Verificá tu email y contraseña.";
            case 403:
                return "Cuenta deshabilitada o sin permisos de acceso.";
            default:
                return "Error del servidor (código " + code + "). Intentá más tarde.";
        }
    }

    /**
     * Traduce excepciones de red a mensajes de error para el usuario.
     */
    private String mapNetworkError(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return "Tiempo de conexión agotado. Verificá tu conexión a internet.";
        }
        if (t instanceof java.io.IOException) {
            return "Error de red. Verificá tu conexión a internet.";
        }
        if (t instanceof com.google.gson.JsonSyntaxException) {
            return "Error de formato en la respuesta del servidor.";
        }
        return "Error inesperado: " + t.getMessage();
    }
}
