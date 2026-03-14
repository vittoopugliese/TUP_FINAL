package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.LocationEntity;

import java.util.List;

@Dao
public interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LocationEntity location);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LocationEntity> locations);

    @Query("SELECT * FROM locations WHERE id = :id")
    LocationEntity getById(String id);

    @Query("SELECT * FROM locations")
    List<LocationEntity> getAll();

    @Query("SELECT * FROM locations WHERE buildingId = :buildingId ORDER BY name")
    List<LocationEntity> getByBuildingId(String buildingId);

    @Query("SELECT COUNT(*) FROM tests t INNER JOIN devices d ON t.deviceId = d.id WHERE d.locationId = :locationId")
    int getTestCountForLocation(String locationId);

    @Query("SELECT COUNT(*) FROM tests t INNER JOIN devices d ON t.deviceId = d.id WHERE d.locationId = :locationId AND t.status = 'COMPLETED'")
    int getCompletedTestCountForLocation(String locationId);

    @Query("SELECT COUNT(*) FROM locations WHERE LOWER(name) = LOWER(:name)")
    int countByName(String name);

    @Query("SELECT COUNT(*) FROM locations WHERE LOWER(name) = LOWER(:name) AND buildingId = :buildingId")
    int countByNameAndBuilding(String name, String buildingId);

    @Update
    void update(LocationEntity location);

    @Query("DELETE FROM locations WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM locations")
    void deleteAll();
}
