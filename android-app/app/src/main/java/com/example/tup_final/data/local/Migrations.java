package com.example.tup_final.data.local;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Room migrations for AppDatabase.
 * Add migrations here when incrementing database version (e.g. MIGRATION_1_2).
 */
public final class Migrations {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE inspections ADD COLUMN locationId TEXT");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_inspections_locationId ON inspections (locationId)");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS inspection_assignments (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "inspectionId TEXT, " +
                    "userEmail TEXT, " +
                    "role TEXT, " +
                    "createdAt TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_assignments_inspectionId ON inspection_assignments (inspectionId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_assignments_userEmail ON inspection_assignments (userEmail)");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE inspections ADD COLUMN buildingName TEXT");
        }
    };

    /** Remove manufacturerId and modelId from devices (no longer needed). */
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("PRAGMA foreign_keys=OFF");
            db.execSQL("CREATE TABLE IF NOT EXISTS devices_new (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "zoneId TEXT, " +
                    "locationId TEXT, " +
                    "buildingId TEXT, " +
                    "deviceTypeId TEXT, " +
                    "deviceCategory TEXT, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "deviceSerialNumber INTEGER, " +
                    "installationDate TEXT, " +
                    "expirationDate TEXT, " +
                    "enabled INTEGER NOT NULL, " +
                    "attributeIds TEXT, " +
                    "createdAt TEXT, " +
                    "updatedAt TEXT, " +
                    "FOREIGN KEY(zoneId) REFERENCES zones(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(locationId) REFERENCES locations(id) ON DELETE CASCADE)");
            db.execSQL("INSERT INTO devices_new (id, zoneId, locationId, buildingId, deviceTypeId, deviceCategory, name, description, deviceSerialNumber, installationDate, expirationDate, enabled, attributeIds, createdAt, updatedAt) " +
                    "SELECT id, zoneId, locationId, buildingId, deviceTypeId, deviceCategory, name, description, deviceSerialNumber, installationDate, expirationDate, enabled, attributeIds, createdAt, updatedAt FROM devices");
            db.execSQL("DROP TABLE devices");
            db.execSQL("ALTER TABLE devices_new RENAME TO devices");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_devices_zoneId ON devices(zoneId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_devices_locationId ON devices(locationId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_devices_buildingId ON devices(buildingId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_devices_deviceCategory ON devices(deviceCategory)");
            db.execSQL("PRAGMA foreign_keys=ON");
        }
    };

    /** Adds GPS and inspector metadata columns to photos table. */
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE photos ADD COLUMN inspectorName TEXT");
            db.execSQL("ALTER TABLE photos ADD COLUMN gpsLatitude REAL");
            db.execSQL("ALTER TABLE photos ADD COLUMN gpsLongitude REAL");
        }
    };

    /** Creates the deficiency_types catalog table. */
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS deficiency_types (" +
                    "id TEXT PRIMARY KEY NOT NULL, " +
                    "code TEXT, " +
                    "name TEXT, " +
                    "description TEXT, " +
                    "category TEXT, " +
                    "sortOrder INTEGER NOT NULL DEFAULT 0)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_deficiency_types_category ON deficiency_types(category)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_deficiency_types_code ON deficiency_types(code)");
        }
    };

    public static final Migration[] ALL = {
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
        MIGRATION_5_6, MIGRATION_6_7
    };

    private Migrations() {}
}
