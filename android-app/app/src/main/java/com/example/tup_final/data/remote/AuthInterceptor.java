package com.example.tup_final.data.remote;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor que añade el token JWT al header Authorization de todas las peticiones.
 * Requerido para que el backend devuelva datos según el rol del usuario autenticado.
 */
public class AuthInterceptor implements Interceptor {

    private static final String PREFS_TOKEN = "cached_token";

    private final SharedPreferences prefs;

    public AuthInterceptor(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = prefs.getString(PREFS_TOKEN, null);
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }
        Request request = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(request);
    }
}
