package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.PhotoEntity;

import java.util.List;

@Dao
public interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PhotoEntity photo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PhotoEntity> photos);

    @Query("SELECT * FROM photos WHERE id = :id")
    PhotoEntity getById(String id);

    @Query("SELECT * FROM photos")
    List<PhotoEntity> getAll();

    @Query("SELECT * FROM photos WHERE stepId = :stepId")
    List<PhotoEntity> getByStepId(String stepId);

    @Query("SELECT * FROM photos WHERE deviceId = :deviceId")
    List<PhotoEntity> getByDeviceId(String deviceId);

    @Update
    void update(PhotoEntity photo);

    @Query("DELETE FROM photos WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM photos")
    void deleteAll();
}
