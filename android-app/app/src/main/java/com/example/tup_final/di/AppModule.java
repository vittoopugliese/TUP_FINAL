package com.example.tup_final.di;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Base Hilt module for application-level dependencies.
 * Add providers here as needed (e.g. Database, Retrofit, SharedPreferences).
 */
@Module
@InstallIn(SingletonComponent.class)
public final class AppModule {
}
