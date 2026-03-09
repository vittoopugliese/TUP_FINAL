package com.example.tup_final.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.tup_final.data.local.AppDatabase;
import com.example.tup_final.data.local.UserDao;
=======
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.tup_final.data.local.AppDatabase;
>>>>>>> 00187f6 (T2.2.1 y T2.2.2: Implementar diálogo de logout y limpieza completa de sesión)
import com.example.tup_final.data.remote.AuthApi;
import com.example.tup_final.ui.forgotpassword.ForgotPasswordViewModel.ResetResult;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;

/**
 * Repository for authentication. Handles login online (via AuthApi) and offline (cached credentials).
 * Also handles forgot-password flow with online/offline fallback.
 */
@Singleton
public class AuthRepository {

    private static final String PREFS_EMAIL = "cached_email";
    private static final String PREFS_PASSWORD_HASH = "cached_password_hash";
    private static final String PREFS_TOKEN = "cached_token";
    private static final String PREFS_USER_ID = "cached_user_id";

    private final AuthApi authApi;
    private final SharedPreferences prefs;
    private final AppDatabase appDatabase;
    private final Context context;
    private final UserDao userDao;

    @Inject
    public AuthRepository(AuthApi authApi, SharedPreferences prefs,
                          AppDatabase appDatabase, @ApplicationContext Context context, UserDao userDao) {
        this.authApi = authApi;
        this.prefs = prefs;
        this.appDatabase = appDatabase;
        this.context = context;
        this.userDao = userDao;
=======
    private final AppDatabase appDatabase;
    private final Context context;

    @Inject
    public AuthRepository(AuthApi authApi, SharedPreferences prefs,
                          AppDatabase appDatabase, @ApplicationContext Context context) {
        this.authApi = authApi;
        this.prefs = prefs;
        this.appDatabase = appDatabase;
        this.context = context;
    }

    /**
     * Performs full logout cleanup. Must be called from a background thread.
     * 1) Notifies server  2) Clears SharedPreferences  3) Clears Room
     * 4) Clears Glide cache  5) Cancels WorkManager tasks
     */
    public void logout() {
        try {
            authApi.logout().execute();
        } catch (IOException ignored) { }

        prefs.edit().clear().apply();

        appDatabase.clearAllTables();

        Glide.get(context).clearDiskCache();
        new Handler(Looper.getMainLooper()).post(() -> Glide.get(context).clearMemory());

        WorkManager.getInstance(context).cancelAllWork();
    }

    /**
     * Attempts login via backend. On success, caches credentials locally.
     *
     * @return JsonObject with token, userId, etc. on success
     * @throws IOException on network error
     */
    public JsonObject loginOnline(String email, String password) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        Response<JsonObject> response = authApi.login(body).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Login failed: " + (response.code()));
        }

        JsonObject result = response.body();
        saveCredentials(email, password, result);
        return result;
    }

    /**
     * Clears cached credentials from SharedPreferences (logout).
     */
    public void logout() {
        prefs.edit().clear().apply();
    }

    /**
     * Validates credentials against cached values (offline mode).
     *
     * @return true if email and password hash match cached credentials
     */
    public boolean loginOffline(String email, String password) {
        String cachedEmail = prefs.getString(PREFS_EMAIL, null);
        String cachedHash = prefs.getString(PREFS_PASSWORD_HASH, null);
        if (cachedEmail == null || cachedHash == null) {
            return false;
        }
        if (!cachedEmail.equalsIgnoreCase(email != null ? email.trim() : "")) {
            return false;
        }
        String inputHash = hashPassword(password);
        return inputHash != null && inputHash.equals(cachedHash);
    }

    /**
     * Tries online login first. On network error, falls back to offline validation.
     *
     * @return JsonObject on success (from backend or built from cache), null on failure
     */
    public JsonObject login(String email, String password) {
        try {
            return loginOnline(email, password);
        } catch (IOException e) {
            if (loginOffline(email, password)) {
                return buildOfflineResult();
            }
            return null;
        }
    }

    // ──────────────────────────────────────────────
    // Forgot Password
    // ──────────────────────────────────────────────

    /**
     * Solicita recuperación de contraseña.
     * Intenta online primero; si falla por red, verifica offline en la BD local.
     *
     * @param email correo electrónico del usuario
     * @return ResetResult indicando el resultado
     */
    public ResetResult requestPasswordReset(String email) {
        try {
            return requestPasswordResetOnline(email);
        } catch (IOException e) {
            return checkEmailExistsOffline(email);
        }
    }

    /**
     * Envía solicitud de recuperación al backend.
     *
     * @throws IOException si hay error de red
     */
    private ResetResult requestPasswordResetOnline(String email) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);

        Response<JsonObject> response = authApi.forgotPassword(body).execute();
        if (response.isSuccessful()) {
            return ResetResult.SUCCESS_ONLINE;
        } else if (response.code() == 404) {
            return ResetResult.ERROR_NOT_FOUND;
        } else {
            throw new IOException("Server error: " + response.code());
        }
        if (!cachedEmail.equalsIgnoreCase(email != null ? email.trim() : "")) {
            return false;
        }
        String inputHash = hashPassword(password);
        return inputHash != null && inputHash.equals(cachedHash);
    }

    /**
     * Verifica si el email existe en la base de datos local (Room).
     * Usado como fallback cuando no hay conexión.
     */
    private ResetResult checkEmailExistsOffline(String email) {
        if (userDao.getByEmail(email != null ? email.trim() : "") != null) {
            return ResetResult.SUCCESS_OFFLINE;
        }
        return ResetResult.ERROR_NETWORK;
    }

    // ──────────────────────────────────────────────

    private void saveCredentials(String email, String password, JsonObject response) {
        String hash = hashPassword(password);
        if (hash == null) return;

        String token = response.has("token") ? response.get("token").getAsString() : "";
        String userId = response.has("userId") ? response.get("userId").getAsString() : "";

        prefs.edit()
                .putString(PREFS_EMAIL, email != null ? email.trim() : "")
                .putString(PREFS_PASSWORD_HASH, hash)
                .putString(PREFS_TOKEN, token)
                .putString(PREFS_USER_ID, userId)
                .apply();
    }

    private JsonObject buildOfflineResult() {
        JsonObject result = new JsonObject();
        result.addProperty("token", prefs.getString(PREFS_TOKEN, ""));
        result.addProperty("userId", prefs.getString(PREFS_USER_ID, ""));
        result.addProperty("offline", true);
        return result;
    }

    private static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}

