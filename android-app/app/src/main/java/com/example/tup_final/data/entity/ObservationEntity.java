package com.example.tup_final.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Observation
 * Nota, recomendación o deficiencia adjunta a un Step.
 * Tipos: RECOMMENDATIONS, REMARKS, DEFICIENCIES.
 * Las deficiencias cambian el estado del Step a FAILED.
 */

@Entity(
    tableName = "observations",
    indices = { @Index("testStepId"), @Index("inspectionId"), @Index("type") },
    foreignKeys = @ForeignKey(
        entity = StepEntity.class,
        parentColumns = "id",
        childColumns = "testStepId",
        onDelete = ForeignKey.CASCADE
    )
)

public class ObservationEntity {
    @PrimaryKey
    public String id;
    /** FK para el Step. */
    public String testStepId;
    /** FK para la Inspection. */
    public String inspectionId;
    /** Nombre a mostrar (tipo en UI). */
    public String name;
    /**
     * Tipo: RECOMMENDATIONS, REMARKS, DEFICIENCIES.
     * La deficiencia afecta el estado del Step.
     */
    public String type;
    /** Descripción. Requerido para DEFICIENCIES. */
    public String description;
    /** Requerido si type=DEFICIENCIES. FK para DeficiencyType. */
    public String deficiencyTypeId;
    /** FK para Photo/Media. */
    public String mediaId;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;
    /** Marca de tiempo de actualización (ISO string). */
    public String updatedAt;

    public ObservationEntity() {}
}