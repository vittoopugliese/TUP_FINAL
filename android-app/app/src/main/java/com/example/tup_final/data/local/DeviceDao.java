package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.tup_final.data.entity.DeviceEntity;

import java.util.List;

@Dao
public interface DeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DeviceEntity device);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertOrIgnore(DeviceEntity device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DeviceEntity> devices);

    @Transaction
    default void upsert(DeviceEntity device) {
        long id = insertOrIgnore(device);
        if (id == -1L) {
            update(device);
        }
    }

    @Query("SELECT * FROM devices WHERE id = :id")
    DeviceEntity getById(String id);

    @Query("SELECT * FROM devices")
    List<DeviceEntity> getAll();

    @Query("SELECT * FROM devices WHERE zoneId = :zoneId ORDER BY name")
    List<DeviceEntity> getByZoneId(String zoneId);

    @Query("SELECT * FROM devices WHERE locationId = :locationId ORDER BY name")
    List<DeviceEntity> getByLocationId(String locationId);

    @Query("SELECT * FROM devices WHERE buildingId = :buildingId ORDER BY name")
    List<DeviceEntity> getByBuildingId(String buildingId);

    @Update
    void update(DeviceEntity device);

    @Query("DELETE FROM devices WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM devices")
    void deleteAll();
}
