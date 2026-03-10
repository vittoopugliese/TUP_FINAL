package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Location.
 * Área física o sala dentro de un edificio. Agrupa zonas y dispositivos.
 * Se usa para filtrar por "activo" (con actividad en inspección) o "todos".
 */

@Entity(
    tableName = "locations",
    indices = { @Index("buildingId"), @Index("name") }
)

public class LocationEntity {
    @PrimaryKey
    @NonNull
    public String id;
    /** FK para el edificio. */
    public String buildingId;
    /** Nombre (e.g. "Sala de emergencias"). */
    public String name;
    /** Descripción adicional. */
    public String details;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;
    /** Marca de tiempo de actualización (ISO string). */
    public String updatedAt;

    public LocationEntity() {}

    public LocationEntity(String id, String buildingId, String name, String details, String createdAt, String updatedAt) {
        this.id = id;
        this.buildingId = buildingId;
        this.name = name;
        this.details = details;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}