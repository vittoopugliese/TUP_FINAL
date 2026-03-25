package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.tup_final.data.entity.InspectionEntity;

import java.util.List;

@Dao
public interface InspectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InspectionEntity inspection);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertOrIgnore(InspectionEntity inspection);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InspectionEntity> inspections);

    @Transaction
    default void upsert(InspectionEntity inspection) {
        long id = insertOrIgnore(inspection);
        if (id == -1L) {
            update(inspection);
        }
    }

    @Query("DELETE FROM inspections WHERE id NOT IN (:ids)")
    void deleteNotIn(List<String> ids);

    @Query("SELECT * FROM inspections WHERE id = :id")
    InspectionEntity getById(String id);

    @Query("SELECT * FROM inspections")
    List<InspectionEntity> getAll();

    @Query("SELECT DISTINCT buildingId FROM inspections WHERE buildingId IS NOT NULL AND buildingId != '' ORDER BY buildingId")
    List<String> getDistinctBuildingIds();

    @Query("SELECT DISTINCT locationId FROM inspections WHERE locationId IS NOT NULL AND locationId != '' ORDER BY locationId")
    List<String> getDistinctLocationIds();

    @Query("SELECT * FROM inspections WHERE buildingId = :buildingId ORDER BY scheduledDate")
    List<InspectionEntity> getByBuildingId(String buildingId);

    @Query("SELECT * FROM inspections WHERE status = :status ORDER BY scheduledDate")
    List<InspectionEntity> getByStatus(String status);

    @Update
    void update(InspectionEntity inspection);

    @Query("DELETE FROM inspections WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM inspections")
    void deleteAll();
}
