package com.example.tup_final;

import android.app.Application;

import com.example.tup_final.sync.SyncScheduler;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application class for Inspections app.
 * Hilt uses this to set up the application-level dependency container.
 */
@HiltAndroidApp
public class InspectionsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SyncScheduler.schedulePeriodic(this);
    }
}
