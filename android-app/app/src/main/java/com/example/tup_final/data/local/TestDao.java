package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.tup_final.data.entity.TestEntity;

import java.util.List;

@Dao
public interface TestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TestEntity test);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertOrIgnore(TestEntity test);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TestEntity> tests);

    @Transaction
    default void upsert(TestEntity test) {
        long id = insertOrIgnore(test);
        if (id == -1L) {
            update(test);
        }
    }

    @Query("SELECT * FROM tests WHERE id = :id")
    TestEntity getById(String id);

    @Query("SELECT * FROM tests")
    List<TestEntity> getAll();

    @Query("SELECT * FROM tests WHERE deviceId = :deviceId ORDER BY name")
    List<TestEntity> getByDeviceId(String deviceId);

    @Query("SELECT * FROM tests WHERE inspectionId = :inspectionId ORDER BY name")
    List<TestEntity> getByInspectionId(String inspectionId);

    @Update
    void update(TestEntity test);

    @Query("DELETE FROM tests WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM tests")
    void deleteAll();
}
