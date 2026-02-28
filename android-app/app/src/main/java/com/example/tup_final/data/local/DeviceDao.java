package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.DeviceEntity;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceEntity device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DeviceEntity> devices);

    @Query("SELECT * FROM devices WHERE id = :id")
    DeviceEntity getById(String id);

    @Query("SELECT * FROM devices")
    List<DeviceEntity> getAll();

    @Query("SELECT * FROM devices WHERE zoneId = :zoneId ORDER BY name")
    List<DeviceEntity> getByZoneId(String zoneId);

    @Query("SELECT * FROM devices WHERE locationId = :locationId ORDER BY name")
    List<DeviceEntity> getByLocationId(String locationId);

    @Update
    void update(DeviceEntity device);

    @Query("DELETE FROM devices WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM devices")
    void deleteAll();
}
