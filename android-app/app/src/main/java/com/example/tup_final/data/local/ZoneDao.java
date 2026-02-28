package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.ZoneEntity;

import java.util.List;

@Dao
public interface ZoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ZoneEntity zone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ZoneEntity> zones);

    @Query("SELECT * FROM zones WHERE id = :id")
    ZoneEntity getById(String id);

    @Query("SELECT * FROM zones")
    List<ZoneEntity> getAll();

    @Query("SELECT * FROM zones WHERE locationId = :locationId ORDER BY name")
    List<ZoneEntity> getByLocationId(String locationId);

    @Update
    void update(ZoneEntity zone);

    @Query("DELETE FROM zones WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM zones")
    void deleteAll();
}
