package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
    * Entidad Room para Inspection.
 * Representa una inspección programada o en progreso en un edificio.
 * Estados: PENDING, IN_PROGRESS, DONE_FAILED, DONE_COMPLETED.
 */

@Entity(
    tableName = "inspections",
    indices = { @Index("buildingId"), @Index("locationId"), @Index("status"), @Index("scheduledDate") }
)

public class InspectionEntity {
    @PrimaryKey
    @NonNull
    public String id;
    /** FK para el edificio. */
    public String buildingId;
    /** Nombre legible del edificio (para mostrar en UI). */
    public String buildingName;
    /** FK para la ubicación (Location). */
    public String locationId;
    /** Tipo: Daily, Weekly, Monthly, Annually. */
    public String type;
    /** Estado: PENDING, IN_PROGRESS, DONE_FAILED, DONE_COMPLETED. */
    public String status;
    /** Fecha programada (ISO string). */
    public String scheduledDate;
    /** Fecha de aprobación (ISO string). */
    public String approvalDate;
    /** Resultado cuando DONE: SUCCESS o FAILED. */
    public String result;
    /** Notas de la inspección. */
    public String notes;
    /** Nombre del firmante. */
    public String signer;
    /** Si el inspector ha firmado la inspección (true) o no (false). */
    public boolean signed;
    /** Fecha de firma (ISO string). */
    public String signDate;
    /** Marca de tiempo cuando se inició la inspección (ISO string). */
    public String startedAt;
    /** FK para el reporte PDF generado. */
    public String inspectionReportId;
    /** FK para el template de inspección. */
    public String inspectionTemplateId;
    /** FK para la página de portada. */
    public String coverPageId;
    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;
    /** Marca de tiempo de actualización (ISO string). */
    public String updatedAt;

    public InspectionEntity() {}
}