package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.ObservationEntity;

import java.util.List;

@Dao
public interface ObservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ObservationEntity observation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ObservationEntity> observations);

    @Query("SELECT * FROM observations WHERE id = :id")
    ObservationEntity getById(String id);

    @Query("SELECT * FROM observations")
    List<ObservationEntity> getAll();

    @Query("SELECT * FROM observations WHERE testStepId = :testStepId ORDER BY createdAt")
    List<ObservationEntity> getByStepId(String testStepId);

    @Query("SELECT * FROM observations WHERE inspectionId = :inspectionId")
    List<ObservationEntity> getByInspectionId(String inspectionId);

    @Update
    void update(ObservationEntity observation);

    @Query("DELETE FROM observations WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM observations")
    void deleteAll();
}
