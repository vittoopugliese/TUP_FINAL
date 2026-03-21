package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tup_final.data.entity.DeficiencyTypeEntity;

import java.util.List;

@Dao
public interface DeficiencyTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DeficiencyTypeEntity> types);

    @Query("SELECT * FROM deficiency_types ORDER BY sortOrder ASC, name ASC")
    List<DeficiencyTypeEntity> getAll();

    @Query("SELECT * FROM deficiency_types WHERE id = :id")
    DeficiencyTypeEntity getById(String id);

    @Query("SELECT * FROM deficiency_types WHERE category = :category ORDER BY sortOrder ASC")
    List<DeficiencyTypeEntity> getByCategory(String category);

    @Query("DELETE FROM deficiency_types")
    void deleteAll();
}
