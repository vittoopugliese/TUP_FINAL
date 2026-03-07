package com.example.tup_final.sync;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.tup_final.data.remote.SyncApi;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * WorkManager Worker que ejecuta la sincronización periódica con el backend.
 * Construye Retrofit localmente (no usa Hilt) para poder ejecutarse en procesos separados.
 */
public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Iniciando sincronización...");

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SyncApi syncApi = retrofit.create(SyncApi.class);

        try {
            JsonObject pushPayload = new JsonObject();
            pushPayload.add("changes", new com.google.gson.JsonArray());
            pushPayload.addProperty("lastSyncAt", "");

            var pushResponse = syncApi.pushChanges(pushPayload).execute();
            if (!pushResponse.isSuccessful()) {
                Log.w(TAG, "Push falló: " + pushResponse.code());
                return Result.retry();
            }

            JsonObject pullPayload = new JsonObject();
            pullPayload.addProperty("lastSyncAt", "");
            var pullResponse = syncApi.pullChanges(pullPayload).execute();
            if (!pullResponse.isSuccessful()) {
                Log.w(TAG, "Pull falló: " + pullResponse.code());
                return Result.retry();
            }

            Log.d(TAG, "Sincronización completada");
            return Result.success();
        } catch (IOException e) {
            Log.e(TAG, "Error de red durante sync", e);
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado durante sync", e);
            return Result.failure();
        }
    }
}
