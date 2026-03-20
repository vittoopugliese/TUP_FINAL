package com.example.tup_final.util;

import androidx.annotation.Nullable;

import java.util.Locale;

/**
 * POJO que encapsula todos los metadatos capturados junto a una foto de observación.
 * Inmutable por diseño.
 */
public class PhotoMetadata {

    /** Ruta local donde quedó guardada la imagen en el dispositivo. */
    public final String localPath;

    /** Timestamp ISO-8601 del momento de captura (Instant.now().toString()). */
    public final String timestamp;

    /** Latitud GPS al momento de capturar; null si sin permiso o sin señal. */
    @Nullable public final Double latitude;

    /** Longitud GPS al momento de capturar; null si sin permiso o sin señal. */
    @Nullable public final Double longitude;

    /** ID del inspector obtenido desde SharedPreferences ("cached_user_id"). */
    public final String inspectorId;

    /** Nombre completo del inspector, resuelto desde UserDao o SharedPreferences. */
    public final String inspectorName;

    public PhotoMetadata(String localPath, String timestamp,
                         @Nullable Double latitude, @Nullable Double longitude,
                         String inspectorId, String inspectorName) {
        this.localPath     = localPath;
        this.timestamp     = timestamp;
        this.latitude      = latitude;
        this.longitude     = longitude;
        this.inspectorId   = inspectorId;
        this.inspectorName = inspectorName;
    }

    public boolean hasGps() {
        return latitude != null && longitude != null;
    }

    @Nullable
    public String formatGps() {
        if (!hasGps()) return null;
        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
    }
}
