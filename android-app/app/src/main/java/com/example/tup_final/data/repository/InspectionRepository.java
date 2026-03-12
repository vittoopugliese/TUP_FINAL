package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.local.InspectionDao;
import com.example.tup_final.data.remote.InspectionApi;
import com.example.tup_final.util.Resource;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Repository para inspecciones.
 * Intenta obtener datos online (InspectionApi), guarda en Room,
 * y usa Room como fallback offline.
 */
@Singleton
public class InspectionRepository {

    private final InspectionApi inspectionApi;
    private final InspectionDao inspectionDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public InspectionRepository(InspectionApi inspectionApi, InspectionDao inspectionDao) {
        this.inspectionApi = inspectionApi;
        this.inspectionDao = inspectionDao;
    }

    /**
     * Obtiene las inspecciones asignadas al usuario.
     * Online: llama al backend, guarda en Room y retorna.
     * Offline: lee de Room como fallback.
     *
     * @return LiveData con el estado del recurso
     */
    public LiveData<Resource<List<InspectionEntity>>> getInspections() {
        MutableLiveData<Resource<List<InspectionEntity>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                Response<List<JsonObject>> response = inspectionApi.getInspections().execute();
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Error al obtener inspecciones: " + response.code());
                }

                List<InspectionEntity> entities = mapJsonListToEntities(response.body());
                inspectionDao.deleteAll();
                if (!entities.isEmpty()) {
                    inspectionDao.insertAll(entities);
                }
                mainHandler.post(() -> result.setValue(Resource.success(entities)));
            } catch (IOException e) {
                List<InspectionEntity> cached = inspectionDao.getAll();
                if (!cached.isEmpty()) {
                    mainHandler.post(() -> result.setValue(Resource.success(cached)));
                } else {
                    mainHandler.post(() -> result.setValue(
                            Resource.error("No se pudieron cargar las inspecciones. Verificá tu conexión.")));
                }
            }
        });

        return result;
    }

    private List<InspectionEntity> mapJsonListToEntities(List<JsonObject> jsonList) {
        List<InspectionEntity> result = new ArrayList<>();
        for (JsonObject json : jsonList) {
            InspectionEntity entity = mapJsonToEntity(json);
            if (entity != null) {
                result.add(entity);
            }
        }
        return result;
    }

    private InspectionEntity mapJsonToEntity(JsonObject json) {
        String id = getStringOrEmpty(json, "id");
        if (id.isEmpty()) {
            return null;
        }
        InspectionEntity entity = new InspectionEntity();
        entity.id = id;
        entity.buildingId = getStringOrEmpty(json, "buildingId");
        entity.type = getStringOrEmpty(json, "type");
        entity.status = getStringOrEmpty(json, "status");
        entity.scheduledDate = getStringOrEmpty(json, "scheduledDate");
        entity.approvalDate = getStringOrEmpty(json, "approvalDate");
        entity.result = getStringOrEmpty(json, "result");
        entity.notes = getStringOrEmpty(json, "notes");
        entity.signer = getStringOrEmpty(json, "signer");
        entity.signed = json.has("signed") && json.get("signed").getAsBoolean();
        entity.signDate = getStringOrEmpty(json, "signDate");
        entity.startedAt = getStringOrEmpty(json, "startedAt");
        entity.inspectionReportId = getStringOrEmpty(json, "inspectionReportId");
        entity.inspectionTemplateId = getStringOrEmpty(json, "inspectionTemplateId");
        entity.coverPageId = getStringOrEmpty(json, "coverPageId");
        entity.createdAt = getStringOrEmpty(json, "createdAt");
        entity.updatedAt = getStringOrEmpty(json, "updatedAt");
        return entity;
    }

    private static String getStringOrEmpty(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return "";
        }
        return json.get(key).getAsString();
    }
}
