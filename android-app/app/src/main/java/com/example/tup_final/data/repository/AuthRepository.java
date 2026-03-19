package com.example.tup_final.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.tup_final.data.local.AppDatabase;
import com.example.tup_final.data.local.UserDao;
import com.example.tup_final.data.remote.AuthApi;
import com.example.tup_final.data.remote.dto.LoginRequest;
import com.example.tup_final.data.remote.dto.LoginResponse;
import com.example.tup_final.data.remote.dto.RegisterRequest;
import com.example.tup_final.data.remote.dto.RegisterResponse;
import com.example.tup_final.ui.forgotpassword.ForgotPasswordViewModel.ResetResult;
import com.example.tup_final.util.Resource;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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
    private static final String PREFS_ROLE = "cached_role";

    private final AuthApi authApi;
    private final SharedPreferences prefs;
    private final AppDatabase appDatabase;
    private final Context context;
    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public AuthRepository(AuthApi authApi, SharedPreferences prefs,
                          AppDatabase appDatabase, @ApplicationContext Context context, UserDao userDao) {
        this.authApi = authApi;
        this.prefs = prefs;
        this.appDatabase = appDatabase;
        this.context = context;
        this.userDao = userDao;
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
        mainHandler.post(() -> Glide.get(context).clearMemory());

        WorkManager.getInstance(context).cancelAllWork();
    }

    /**
     * Attempts login via backend. On success, caches credentials locally.
     *
     * @return LoginResponse on success
     * @throws IOException on network error
     */
    private LoginResponse loginOnline(String email, String password) throws IOException {
        LoginRequest request = new LoginRequest(email, password);
        Response<LoginResponse> response = authApi.login(request).execute();
        if (!response.isSuccessful() || response.body() == null) {
            throw new IOException("Login failed: " + response.code());
        }

        LoginResponse result = response.body();
        saveCredentials(email, password, result);
        return result;
    }

    /**
     * Validates credentials against cached values (offline mode).
     *
     * @return true if email and password hash match cached credentials
     */
    private boolean loginOffline(String email, String password) {
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
     * Returns LiveData for UI observation.
     */
    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                LoginResponse response = loginOnline(email, password);
                mainHandler.post(() -> result.setValue(Resource.success(response)));
            } catch (IOException e) {
                if (loginOffline(email, password)) {
                    LoginResponse offlineResponse = buildOfflineResult();
                    mainHandler.post(() -> result.setValue(Resource.success(offlineResponse)));
                } else {
                    mainHandler.post(() -> result.setValue(
                            Resource.error(e.getMessage() != null ? e.getMessage() : "Login failed")));
                }
            }
        });

        return result;
    }

    // ──────────────────────────────────────────────
    // Register
    // ──────────────────────────────────────────────

    /**
     * Registra un nuevo usuario en el backend.
     *
     * @return LiveData con Resource de RegisterResponse en éxito, o error con mensaje
     */
    public LiveData<Resource<RegisterResponse>> register(String email, String fullName, String password) {
        MutableLiveData<Resource<RegisterResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                RegisterRequest request = new RegisterRequest(
                        email != null ? email.trim() : "",
                        fullName != null ? fullName.trim() : "",
                        "INSPECTOR",
                        password
                );
                Response<RegisterResponse> response = authApi.register(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> result.setValue(Resource.success(response.body())));
                } else {
                    String errorMsg = "Error al registrar";
                    if (response.errorBody() != null) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            com.google.gson.JsonObject json = gson.fromJson(response.errorBody().string(), com.google.gson.JsonObject.class);
                            if (json.has("error")) {
                                errorMsg = json.get("error").getAsString();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    final String msg = errorMsg;
                    mainHandler.post(() -> result.setValue(Resource.error(msg)));
                }
            } catch (IOException e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error de conexión";
                mainHandler.post(() -> result.setValue(Resource.error(msg)));
            }
        });

        return result;
    }

    // ──────────────────────────────────────────────
    // Forgot Password
    // ──────────────────────────────────────────────

    /**
     * Solicita recuperación de contraseña.
     * Intenta online primero; si falla por red, verifica offline en la BD local.
     * Todas las llamadas a Room (userDao) se ejecutan en executor para evitar crash en main thread.
     *
     * @param email    correo electrónico del usuario
     * @param callback recibe el ResetResult cuando termina (se invoca en main thread)
     */
    public void requestPasswordReset(String email, Consumer<ResetResult> callback) {
        executor.execute(() -> {
            try {
                ResetResult result = requestPasswordResetOnline(email);
                mainHandler.post(() -> callback.accept(result));
            } catch (IOException e) {
                ResetResult result = checkEmailExistsOffline(email);
                mainHandler.post(() -> callback.accept(result));
            }
        });
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

    private void saveCredentials(String email, String password, LoginResponse response) {
        String hash = hashPassword(password);
        if (hash == null) return;

        String token = response != null && response.getToken() != null ? response.getToken() : "";
        String userId = response != null && response.getUserId() != null ? response.getUserId() : "";

        String role = response != null && response.getRole() != null ? response.getRole() : "INSPECTOR";
        prefs.edit()
                .putString(PREFS_EMAIL, email != null ? email.trim() : "")
                .putString(PREFS_PASSWORD_HASH, hash)
                .putString(PREFS_TOKEN, token)
                .putString(PREFS_USER_ID, userId)
                .putString(PREFS_ROLE, role)
                .apply();
    }

    private LoginResponse buildOfflineResult() {
        return new LoginResponse(
                prefs.getString(PREFS_TOKEN, ""),
                "Bearer",
                prefs.getString(PREFS_EMAIL, ""),
                prefs.getString(PREFS_ROLE, "INSPECTOR"),
                prefs.getString(PREFS_USER_ID, ""),
                null
        );
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
