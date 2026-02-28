package com.example.tup_final.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Test.
 * Procedimiento de verificación asociado a un dispositivo dentro de una inspección.
 * Contiene una lista de Steps que el inspector debe completar.
 * Estados: PENDING, COMPLETED, FAILED.
 */

@Entity(
    tableName = "tests",
    indices = {@Index("deviceId"), @Index("inspectionId"), @Index("status")},
    foreignKeys = {
        @ForeignKey(
            entity = DeviceEntity.class,
            parentColumns = "id",
            childColumns = "deviceId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = InspectionEntity.class,
            parentColumns = "id",
            childColumns = "inspectionId",
            onDelete = ForeignKey.CASCADE
        )
    }
)

public class TestEntity {
    @PrimaryKey
    public String id;
    /** FK para el Device. */
    public String deviceId;
    /** FK para la Inspection. */
    public String inspectionId;
    /** FK para el template de test. */
    public String testTemplateId;
    /** IDs de los Steps en orden (JSON array string). */
    public String testStepIds;
    public String name;
    /** Descripción del test. */
    public String description;
    /** Estado: PENDING, COMPLETED, FAILED. */
    public String status;
    /** Si el test aplica (N/A en el nivel de test). */
    public boolean applicable = true;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;
    /** Marca de tiempo de actualización (ISO string). */
    public String updatedAt;

    public TestEntity() {}
}