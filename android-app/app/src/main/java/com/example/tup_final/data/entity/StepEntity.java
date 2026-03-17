package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Step (Test Step).
 * Elemento individual de un Test que requiere entrada de datos o validación.
 * Tipos editables: BINARY, DATE_RANGE, SIMPLE_VALUE, NUMERIC_RANGE, MULTI_VALUE.
 * RANGE legacy se mapea a NUMERIC_RANGE. AUTOMATIC fuera de alcance.
 * Estados: PENDING, COMPLETED, FAILED (SUCCESS legacy -> COMPLETED).
 * N/A: applicable=false.
 */

@Entity(
    tableName = "steps",
    indices = {@Index("testId"), @Index("status")},
    foreignKeys = @ForeignKey(
        entity = TestEntity.class,
        parentColumns = "id",
        childColumns = "testId",
        onDelete = ForeignKey.CASCADE
    )
)

public class StepEntity {
    @PrimaryKey
    @NonNull
    public String id;
    /** FK para el Test. */
    public String testId;
    public String name;
    /** Tipo: BINARY, DATE_RANGE, SIMPLE_VALUE, NUMERIC_RANGE, MULTI_VALUE, RANGE(legacy). */
    public String testStepType;
    /** Si el step aplica (N/A en el nivel de step). */
    public boolean applicable = true;
    /** Estado: PENDING, COMPLETED, FAILED. */
    public String status;
    /** Descripción del step. */
    public String description;
    /**
     * Valor almacenado como JSON. Ver docs/step-types-contract.md.
     * - BINARY: { "value": boolean, "valueType": "BOOLEAN_VALUE" }
     * - DATE_RANGE: { "from": "ISO_DATE", "to": "ISO_DATE", "valueType": "DATE_RANGE_VALUE" }
     * - SIMPLE_VALUE: { "value": ..., "valueType": "STRING_VALUE"|"NUMERIC_VALUE"|"DATE_VALUE" }
     * - NUMERIC_RANGE: { "value": number } + minValue/maxValue columnas
     * - MULTI_VALUE: { "values": [ { "name", "value", "valueType" } ] }
     */
    public String valueJson;
    /** Valor mínimo para el tipo RANGE (numérico). */
    public Double minValue;
    /** Valor máximo para el tipo RANGE (numérico). */
    public Double maxValue;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;
    /** Marca de tiempo de actualización (ISO string). */
    public String updatedAt;

    public StepEntity() {}
}