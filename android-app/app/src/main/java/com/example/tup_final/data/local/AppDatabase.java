package com.example.tup_final.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.tup_final.data.entity.AuditLogEntity;
import com.example.tup_final.data.entity.DeficiencyTypeEntity;
import com.example.tup_final.data.entity.DeviceEntity;
import com.example.tup_final.data.entity.InspectionAssignmentEntity;
import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.entity.LocationEntity;
import com.example.tup_final.data.entity.ObservationEntity;
import com.example.tup_final.data.entity.PhotoEntity;
import com.example.tup_final.data.entity.StepEntity;
import com.example.tup_final.data.entity.TestEntity;
import com.example.tup_final.data.entity.UserEntity;
import com.example.tup_final.data.entity.ZoneEntity;

/**
 * Room database for Inspections app.
 * Contains all entities: User, Inspection, Location, Zone, Device, Test, Step,
 * Observation, Photo, AuditLog, DeficiencyType.
 *
 * Version history:
 *  5 → 6: PhotoEntity new columns (gpsLatitude, gpsLongitude, inspectorName)
 *  6 → 7: DeficiencyTypeEntity new table
 */
@Database(
    entities = {
        UserEntity.class,
        InspectionEntity.class,
        InspectionAssignmentEntity.class,
        LocationEntity.class,
        ZoneEntity.class,
        DeviceEntity.class,
        TestEntity.class,
        StepEntity.class,
        ObservationEntity.class,
        PhotoEntity.class,
        AuditLogEntity.class,
        DeficiencyTypeEntity.class
    },
    version = 7,
    exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract InspectionDao inspectionDao();
    public abstract InspectionAssignmentDao inspectionAssignmentDao();
    public abstract LocationDao locationDao();
    public abstract ZoneDao zoneDao();
    public abstract DeviceDao deviceDao();
    public abstract TestDao testDao();
    public abstract StepDao stepDao();
    public abstract ObservationDao observationDao();
    public abstract PhotoDao photoDao();
    public abstract AuditLogDao auditLogDao();
    public abstract DeficiencyTypeDao deficiencyTypeDao();
}
