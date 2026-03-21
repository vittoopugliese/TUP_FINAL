package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para el catálogo de tipos de deficiencia.
 * Cacheado localmente desde GET /api/deficiency-types.
 */
@Entity(
    tableName = "deficiency_types",
    indices = { @Index("category"), @Index("code") }
)
public class DeficiencyTypeEntity {

    @PrimaryKey
    @NonNull
    public String id;

    /** Código corto único, e.g. "EXT_VENCIDO". */
    public String code;

    /** Nombre legible para mostrar en la UI. */
    public String name;

    /** Descripción opcional. */
    public String description;

    /** Categoría de agrupación, e.g. "EXTINCIÓN", "SEÑALIZACIÓN". */
    public String category;

    /** Orden de presentación. */
    public int sortOrder;

    public DeficiencyTypeEntity() {}
}
