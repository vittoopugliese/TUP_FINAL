package com.example.tup_final.data.remote;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor que agrega el header Authorization Bearer con el JWT
 * a todas las peticiones excepto login y register.
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
        String path = original.url().encodedPath();

        // No agregar token a login ni register
        if (path.contains("auth/login") || path.contains("auth/register")) {
            return chain.proceed(original);
        }

        String token = prefs.getString(PREFS_TOKEN, null);
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }

        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token);
        return chain.proceed(builder.build());
    }
}
