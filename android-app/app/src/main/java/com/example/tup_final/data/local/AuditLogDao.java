package com.example.tup_final.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tup_final.data.entity.AuditLogEntity;

import java.util.List;

@Dao
public interface AuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AuditLogEntity auditLog);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AuditLogEntity> auditLogs);

    @Query("SELECT * FROM audit_logs WHERE id = :id")
    AuditLogEntity getById(String id);

    @Query("SELECT * FROM audit_logs")
    List<AuditLogEntity> getAll();

    @Query("SELECT * FROM audit_logs WHERE entityType = :entityType AND entityId = :entityId ORDER BY createdAt DESC")
    List<AuditLogEntity> getByEntity(String entityType, String entityId);

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    List<AuditLogEntity> getByUserId(String userId, int limit);

    @Update
    void update(AuditLogEntity auditLog);

    @Query("DELETE FROM audit_logs WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM audit_logs")
    void deleteAll();
}
