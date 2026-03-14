package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para asignaciones de usuarios a inspecciones.
 * Rol: INSPECTOR u OPERATOR.
 */
@Entity(
    tableName = "inspection_assignments",
    indices = { @Index("inspectionId"), @Index("userEmail") }
)
public class InspectionAssignmentEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String inspectionId;
    public String userEmail;
    public String role;
    public String createdAt;

    public InspectionAssignmentEntity() {
        this.id = "";
    }
}
