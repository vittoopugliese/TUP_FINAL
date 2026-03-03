package com.example.tup_final.di;

import android.content.Context;

import androidx.room.Room;

import com.example.tup_final.data.local.AppDatabase;
import com.example.tup_final.data.local.Migrations;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    @Provides
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "app_database")
                .addMigrations(Migrations.ALL)
                .build();
    }
}
