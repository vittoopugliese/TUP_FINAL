package com.example.tup_final.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.tup_final.data.entity.AuditLogEntity;
import com.example.tup_final.data.entity.DeviceEntity;
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
 * Contains all entities: User, Inspection, Location, Zone, Device, Test, Step, Observation, Photo, AuditLog.
 *
 * @see T1.1.1-Plan-Entidades.md
 */
@Database(
    entities = {
        UserEntity.class,
        InspectionEntity.class,
        LocationEntity.class,
        ZoneEntity.class,
        DeviceEntity.class,
        TestEntity.class,
        StepEntity.class,
        ObservationEntity.class,
        PhotoEntity.class,
        AuditLogEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract InspectionDao inspectionDao();
    public abstract LocationDao locationDao();
    public abstract ZoneDao zoneDao();
    public abstract DeviceDao deviceDao();
    public abstract TestDao testDao();
    public abstract StepDao stepDao();
    public abstract ObservationDao observationDao();
    public abstract PhotoDao photoDao();
    public abstract AuditLogDao auditLogDao();
}
