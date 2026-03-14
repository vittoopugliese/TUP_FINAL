package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.tup_final.data.entity.InspectionAssignmentEntity;

import java.util.List;

@Dao
public interface InspectionAssignmentDao {

    @Query("SELECT * FROM inspection_assignments WHERE inspectionId = :inspectionId ORDER BY role, userEmail")
    List<InspectionAssignmentEntity> getByInspectionId(String inspectionId);

    @Query("SELECT * FROM inspection_assignments WHERE inspectionId = :inspectionId AND role = :role")
    List<InspectionAssignmentEntity> getByInspectionIdAndRole(String inspectionId, String role);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(InspectionAssignmentEntity assignment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InspectionAssignmentEntity> assignments);

    @Query("DELETE FROM inspection_assignments WHERE inspectionId = :inspectionId AND userEmail = :userEmail")
    void deleteByInspectionIdAndEmail(String inspectionId, String userEmail);

    @Query("DELETE FROM inspection_assignments WHERE inspectionId = :inspectionId")
    void deleteByInspectionId(String inspectionId);
}
