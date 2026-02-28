package com.example.tup_final.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Zone.
 * Agrupación lógica de dispositivos dentro de una ubicación (e.g. "Equipo médico", "Sistemas de seguridad").
 * Los dispositivos se cargan de forma perezosa cuando se expande la zona.
 */

@Entity(
    tableName = "zones",
    indices = { @Index("locationId") },
    foreignKeys = @ForeignKey(
        entity = LocationEntity.class,
        parentColumns = "id",
        childColumns = "locationId",
        onDelete = ForeignKey.CASCADE
    )
)

public class ZoneEntity {
    @PrimaryKey
    public String id;
    /** FK para la Location. */
    public String locationId;
    public String name;
    /** Descripción adicional. */
    public String details;

    public ZoneEntity() {}

    public ZoneEntity(String id, String locationId, String name, String details) {
        this.id = id;
        this.locationId = locationId;
        this.name = name;
        this.details = details;
    }
}