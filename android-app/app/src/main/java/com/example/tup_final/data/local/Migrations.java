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
                    "inspectionId TEXT NOT NULL, " +
                    "userEmail TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "createdAt TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_assignments_inspectionId ON inspection_assignments (inspectionId)");
            db.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_assignments_userEmail ON inspection_assignments (userEmail)");
        }
    };

    public static final Migration[] ALL = { MIGRATION_1_2, MIGRATION_2_3 };

    private Migrations() {}
}
