package com.example.tup_final.sync;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Programa la ejecución periódica y on-demand de SyncWorker.
 */
public final class SyncScheduler {

    private static final String WORK_NAME = "sync";
    private static final long INTERVAL_MINUTES = 15;

    private static final Constraints CONSTRAINTS = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    private SyncScheduler() {}

    /**
     * Programa la sincronización periódica cada 15 min. Solo se ejecuta con red conectada.
     * Si ya existe un trabajo con el mismo nombre, se mantiene el existente (KEEP).
     */
    public static void schedulePeriodic(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                INTERVAL_MINUTES,
                TimeUnit.MINUTES
        ).setConstraints(CONSTRAINTS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    /**
     * Encola una sincronización inmediata on-demand.
     */
    public static void enqueueOneTime(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(CONSTRAINTS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(
                "sync_onetime",
                ExistingWorkPolicy.REPLACE,
                request
        );
    }
}
