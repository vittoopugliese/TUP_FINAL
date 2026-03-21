package com.example.tup_final.util;

import androidx.annotation.Nullable;

/**
 * Metadatos capturados junto con una foto de evidencia:
 * ruta local, timestamp ISO, coordenadas GPS e información del inspector.
 */
public class PhotoMetadata {

    /** Ruta absoluta al archivo de imagen en el dispositivo. */
    public final String localPath;

    /** Timestamp ISO-8601 del momento de captura (e.g. "2024-03-17T14:30:22Z"). */
    public final String timestamp;

    /** Latitud GPS; null si el permiso no fue concedido o la señal no estaba disponible. */
    @Nullable
    public final Double latitude;

    /** Longitud GPS; null si el permiso no fue concedido o la señal no estaba disponible. */
    @Nullable
    public final Double longitude;

    /** ID del usuario inspector (de SharedPreferences "cached_user_id"). */
    public final String inspectorId;

    /** Nombre completo del inspector (firstName + lastName de UserEntity). */
    public final String inspectorName;

    public PhotoMetadata(String localPath, String timestamp,
                         @Nullable Double latitude, @Nullable Double longitude,
                         String inspectorId, String inspectorName) {
        this.localPath = localPath;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.inspectorId = inspectorId;
        this.inspectorName = inspectorName;
    }

    public boolean hasGps() {
        return latitude != null && longitude != null;
    }

    /** Devuelve las coordenadas formateadas para mostrar en UI, o null si no hay GPS. */
    @Nullable
    public String formatGps() {
        if (!hasGps()) return null;
        return String.format("%.6f°, %.6f°", latitude, longitude);
    }
}
