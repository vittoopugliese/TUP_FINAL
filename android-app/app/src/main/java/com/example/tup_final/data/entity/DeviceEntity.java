package com.example.tup_final.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**     
 * Entidad Room para Device.
 * Equipo físico que requiere tests. Tiene categorías especializadas
 * (FACP, Jockey Pump, Fire Pump, etc.) con campos específicos por tipo.
 * (Las categorías no las inventamos, se usan en varios países en los sistemas contra incendios)
 * ***A jockey pump is a small pump connected to a fire sprinkler system to maintain pressure in the sprinkler pipes
 */

@Entity(
    tableName = "devices",
    indices = { @Index("zoneId"), @Index("locationId"), @Index("buildingId"), @Index("companyId"), @Index("deviceCategory") },
    foreignKeys = {
        @ForeignKey(
            entity = ZoneEntity.class,
            parentColumns = "id",
            childColumns = "zoneId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = LocationEntity.class,
            parentColumns = "id",
            childColumns = "locationId",
            onDelete = ForeignKey.CASCADE
        )
    }
)

public class DeviceEntity {
    @PrimaryKey
    public String id;
    public String zoneId;
    public String locationId;
    public String buildingId;
    /** FK para el fabricante. */
    public String manufacturerId;
    /** FK para el modelo. */
    public String modelId;
    /** FK para el tipo de dispositivo. */
    public String deviceTypeId;
    /**
     * Categoría: FA_FIELD_DEVICE, FACP_DEVICE, JOCKEY_PUMP, FIRE_PUMP,
     * PUMP_CONTROLLER, SPRINKLER_DEVICE, etc.
     */
    public String deviceCategory;
    public String name;
    public String description;
    public Integer deviceSerialNumber;
    /** Fecha de instalación (ISO string). */
    public String installationDate;
    /** Fecha de expiración (ISO string). */
    public String expirationDate;
    /** Si el dispositivo está habilitado (true) o no (false). */
    public boolean enabled = true;
    /** IDs de atributos adicionales (JSON array string). */
    public String attributeIds;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;
    /** Marca de tiempo de actualización (ISO string). */
    public String updatedAt;

    public DeviceEntity() {}
}