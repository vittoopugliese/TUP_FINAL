package com.example.tup_final.data.repository;

import android.content.SharedPreferences;

import com.example.tup_final.data.remote.AuthApi;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository for authentication. Handles login online (via AuthApi) and offline (cached credentials).
 */
@Singleton
public class AuthRepository {

    private static final String PREFS_EMAIL = "cached_email";
    private static final String PREFS_PASSWORD_HASH = "cached_password_hash";
    private static final String PREFS_TOKEN = "cached_token";
    private static final String PREFS_USER_ID = "cached_user_id";

    private final AuthApi authApi;
    private final SharedPreferences prefs;

    @Inject
    public AuthRepository(AuthApi authApi, SharedPreferences prefs) {
        this.authApi = authApi;
        this.prefs = prefs;
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
