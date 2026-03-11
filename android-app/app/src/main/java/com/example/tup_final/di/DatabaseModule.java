package com.example.tup_final.di;

import android.content.Context;

import androidx.room.Room;

import com.example.tup_final.data.local.AppDatabase;
import com.example.tup_final.data.local.InspectionDao;
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
}
