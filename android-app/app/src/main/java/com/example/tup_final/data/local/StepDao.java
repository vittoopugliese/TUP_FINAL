package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.StepEntity;

import java.util.List;

@Dao
public interface StepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StepEntity step);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StepEntity> steps);

    @Query("SELECT * FROM steps WHERE id = :id")
    StepEntity getById(String id);

    @Query("SELECT * FROM steps")
    List<StepEntity> getAll();

    @Query("SELECT * FROM steps WHERE testId = :testId ORDER BY createdAt")
    List<StepEntity> getByTestId(String testId);

    @Update
    void update(StepEntity step);

    @Query("DELETE FROM steps WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM steps")
    void deleteAll();
}
