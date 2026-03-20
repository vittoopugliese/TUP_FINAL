package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Photo (Media).
 * Imagen subida como evidencia. Almacenada en un servicio de medios separado.
 * Las Observaciones referencian Photo via mediaId.
 */

@Entity(
    tableName = "photos",
    indices = { @Index("mediaUrl") }
)

public class PhotoEntity {

    @PrimaryKey
    @NonNull
    public String id;
    /** URL de descarga/vista. */
    public String mediaUrl;
    /** Nombre del archivo. */
    public String name;
    /** Descripción adicional. */
    public String description;
    /** Detalles del archivo como JSON. E.g. { "size": number }. */
    public String fileDetailsJson;
    /** Ruta local del archivo cuando se almacena offline (antes de la sincronización). */
    public String localPath;
    /** Marca de tiempo cuando se tomó la foto (ISO string). */
    public String timestamp;
    /** ID del inspector que capturó la foto. */
    public String inspectorId;
    /** Nombre completo del inspector que capturó la foto. */
    public String inspectorName;
    /** Latitud GPS al momento de la captura (null si no disponible). */
    public Double gpsLatitude;
    /** Longitud GPS al momento de la captura (null si no disponible). */
    public Double gpsLongitude;
    /** FK para el Step (para enlace automático). */
    public String stepId;
    /** FK para el Device (para enlace automático). */
    public String deviceId;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;

    // ── Metadata de captura (agregado en versión 6) ────────────────────────────
    /** Nombre completo del inspector que capturó la foto. */
    public String inspectorName;
    /** Latitud GPS en el momento de la captura. Null si sin permiso/señal. */
    public Double gpsLatitude;
    /** Longitud GPS en el momento de la captura. Null si sin permiso/señal. */
    public Double gpsLongitude;

    public PhotoEntity() {}
}