package com.example.tup_final.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para Step (Test Step).
 * Elemento individual de un Test que requiere entrada de datos o validación.
 * Tipos: BINARY, RANGE, SIMPLE_VALUE, MULTI_VALUE, AUTOMATIC.
 * Puede tener Observaciones (anotaciones) adjuntas.
 * Estados: PENDING, SUCCESS, FAILED.
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
    public String id;
    /** FK para el Test. */
    public String testId;
    public String name;
    /** Tipo: BINARY, RANGE, SIMPLE_VALUE, MULTI_VALUE, AUTOMATIC. */
    public String testStepType;
    /** Si el step aplica (N/A en el nivel de step). */
    public boolean applicable = true;
    /** Estado: PENDING, SUCCESS, FAILED. */
    public String status;
    /** Descripción del step. */
    public String description;
    /**
     * Valor almacenado como JSON. La estructura depende del testStepType:
     * - BINARY: { "value": boolean, "valueType": "BOOLEAN_VALUE" }
     * - RANGE: minValue, maxValue (NumericUnitValue o DateValue)
     * - SIMPLE_VALUE: StringValue, NumericUnitValue, DateValue o BooleanValue
     * - MULTI_VALUE: array de { name, value, valueType, measureUnitId? }
     * - AUTOMATIC: formula, dependsOn, value (solo lectura)
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