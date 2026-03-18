package com.example.tup_final.di;

import android.content.Context;

import androidx.room.Room;

import com.example.tup_final.data.local.AppDatabase;
import com.example.tup_final.data.local.DeviceDao;
import com.example.tup_final.data.local.InspectionAssignmentDao;
import com.example.tup_final.data.local.InspectionDao;
import com.example.tup_final.data.local.LocationDao;
import com.example.tup_final.data.local.ObservationDao;
import com.example.tup_final.data.local.StepDao;
import com.example.tup_final.data.local.TestDao;
import com.example.tup_final.data.local.ZoneDao;
import com.example.tup_final.data.local.Migrations;
import com.example.tup_final.data.local.UserDao;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "app_database")
                .addMigrations(Migrations.ALL)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public static UserDao provideUserDao(AppDatabase db) {
        return db.userDao();
    }

    @Provides
    public static InspectionDao provideInspectionDao(AppDatabase db) {
        return db.inspectionDao();
    }

    @Provides
    public static InspectionAssignmentDao provideInspectionAssignmentDao(AppDatabase db) {
        return db.inspectionAssignmentDao();
    }

    @Provides
    public static DeviceDao provideDeviceDao(AppDatabase db) {
        return db.deviceDao();
    }

    @Provides
    public static LocationDao provideLocationDao(AppDatabase db) {
        return db.locationDao();
    }

    @Provides
    public static ZoneDao provideZoneDao(AppDatabase db) {
        return db.zoneDao();
    }

    @Provides
    public static TestDao provideTestDao(AppDatabase db) {
        return db.testDao();
    }

    @Provides
    public static StepDao provideStepDao(AppDatabase db) {
        return db.stepDao();
    }

    @Provides
    public static ObservationDao provideObservationDao(AppDatabase db) {
        return db.observationDao();
    }
}
