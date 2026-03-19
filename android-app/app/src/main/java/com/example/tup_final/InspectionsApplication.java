package com.example.tup_final;

import android.app.Application;
import android.util.Log;

import com.example.tup_final.sync.SyncScheduler;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Application class for Inspections app.
 * Hilt uses this to set up the application-level dependency container.
 */
@HiltAndroidApp
public class InspectionsApplication extends Application {

    private static final String TAG = "STARTUP_ERROR";

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            // SyncWorker bloqueaba el inicio si el backend no responde. Descomentar cuando el servidor esté estable.
            // SyncScheduler.schedulePeriodic(this);
        } catch (Throwable t) {
            Log.e(TAG, "Error durante el inicio de la aplicación", t);
            throw t;
        }
    }
}
