package com.example.tup_final.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tup_final.data.entity.InspectionEntity;
import com.example.tup_final.data.local.InspectionDao;
import com.example.tup_final.data.remote.InspectionApi;
import com.example.tup_final.data.remote.dto.InspectionListResponse;
import com.example.tup_final.util.Resource;

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
 * Patrón offline-first: intenta online, cachea en Room, fallback a Room.
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
                Response<List<InspectionListResponse>> response = inspectionApi.getInspections().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<InspectionEntity> entities = mapToEntities(response.body());
                    inspectionDao.deleteAll();
                    if (!entities.isEmpty()) {
                        inspectionDao.insertAll(entities);
                    }
                    mainHandler.post(() -> result.setValue(Resource.success(entities)));
                } else {
                    // Server error, try to load from cache
                    loadFromCache(result);
                }
            } catch (IOException e) {
                // Network error, try to load from cache
                loadFromCache(result);
            }
        });

        return result;
    }

    private void loadFromCache(MutableLiveData<Resource<List<InspectionEntity>>> result) {
        List<InspectionEntity> cached = inspectionDao.getAll();
        if (cached != null && !cached.isEmpty()) {
            mainHandler.post(() -> result.setValue(Resource.success(cached)));
        } else {
            mainHandler.post(() -> result.setValue(
                    Resource.error("No se pudieron cargar las inspecciones. Verificá tu conexión.")));
        }
    }

    private List<InspectionEntity> mapToEntities(List<InspectionListResponse> dtos) {
        List<InspectionEntity> entities = new ArrayList<>();
        for (InspectionListResponse dto : dtos) {
            InspectionEntity entity = new InspectionEntity();
            entity.id = dto.getId() != null ? dto.getId() : "";
            entity.buildingId = dto.getBuildingId();
            entity.type = dto.getType();
            entity.status = dto.getStatus();
            entity.scheduledDate = dto.getScheduledDate();
            // Campos opcionales no presentes en el DTO mínimo - se dejan null
            entity.approvalDate = null;
            entity.result = null;
            entity.notes = null;
            entity.signer = null;
            entity.signed = false;
            entity.signDate = null;
            entity.startedAt = null;
            entity.inspectionReportId = null;
            entity.inspectionTemplateId = null;
            entity.coverPageId = null;
            entity.createdAt = null;
            entity.updatedAt = null;
            entity.locationId = dto.getLocationId();
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Obtiene los IDs de edificios únicos para poblar el filtro.
     *
     * @return LiveData con Resource que contiene la lista de buildingIds
     */
    public LiveData<Resource<List<String>>> getDistinctBuildingIds() {
        MutableLiveData<Resource<List<String>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                List<String> buildingIds = inspectionDao.getDistinctBuildingIds();
                mainHandler.post(() -> result.setValue(Resource.success(buildingIds)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar edificios")));
            }
        });

        return result;
    }

    /**
     * Obtiene los IDs de ubicaciones únicos para poblar el filtro.
     *
     * @return LiveData con Resource que contiene la lista de locationIds
     */
    public LiveData<Resource<List<String>>> getDistinctLocationIds() {
        MutableLiveData<Resource<List<String>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        executor.execute(() -> {
            try {
                List<String> locationIds = inspectionDao.getDistinctLocationIds();
                mainHandler.post(() -> result.setValue(Resource.success(locationIds)));
            } catch (Exception e) {
                mainHandler.post(() -> result.setValue(
                        Resource.error(e.getMessage() != null ? e.getMessage() : "Error al cargar ubicaciones")));
            }
        });

        return result;
    }
}